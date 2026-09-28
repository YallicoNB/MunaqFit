package com.munaqfit.backend.service;

import com.munaqfit.backend.model.DetalleVenta;
import com.munaqfit.backend.model.MovimientoInventario;
import com.munaqfit.backend.model.Parametro;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.Receta;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.repository.MovimientoInventarioRepository;
import com.munaqfit.backend.repository.ParametroRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.RecetaRepository;
import com.munaqfit.backend.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VentaService {

    /** Codigo del parametro que guarda la tasa de IGV vigente. */
    private static final String PARAMETRO_IGV = "IGV";

    /** Formato de fecha del numero de pedido: PED-20260928-0001 */
    private static final DateTimeFormatter FORMATO_FECHA_PEDIDO = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Se usa solo si el parametro no esta cargado en la base. */
    private static final BigDecimal IGV_POR_DEFECTO = new BigDecimal("0.18");

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ParametroRepository parametroRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    /**
     * Registra una venta completa: calcula el IGV y el total, guarda los
     * detalles, descuenta el stock de los insumos segun la receta de cada
     * bebida y deja el movimiento registrado en el kardex.
     */
    @Transactional
    public Venta procesarVenta(Venta venta, List<DetalleVenta> detalles) {

        if (venta == null) {
            throw new IllegalArgumentException("No se envio la informacion de la venta");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto");
        }
        // El usuario lo resuelve el controller a partir del token JWT
        if (venta.getUsuario() == null || venta.getUsuario().getId() == null) {
            throw new IllegalArgumentException("No se pudo determinar el empleado que registra la venta");
        }

        // ---- 1. Calcular el subtotal de cada detalle ----
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVenta detalle : detalles) {

            if (detalle.getBebida() == null || detalle.getBebida().getId() == null) {
                throw new IllegalArgumentException("Cada detalle debe referenciar una bebida existente");
            }
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a cero");
            }

            // Si el frontend no manda el precio, se toma el del catalogo
            BigDecimal precioUnitario = detalle.getPrecioUnitario() != null
                    ? detalle.getPrecioUnitario()
                    : detalle.getBebida().getPrecio();

            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(precioUnitario.multiply(BigDecimal.valueOf(detalle.getCantidad())));
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        // ---- 2. Calcular IGV y total ----
        // La tasa sale de la tabla parametro: antes estaba fija en el codigo
        // y cambiarla exigia recompilar y volver a desplegar.
        BigDecimal igvTasa = obtenerTasaIgv();
        BigDecimal subtotalRedondeado = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv = subtotalRedondeado.multiply(igvTasa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotalRedondeado.add(igv).setScale(2, RoundingMode.HALF_UP);

        venta.setSubtotal(subtotalRedondeado);
        venta.setIgvTasa(igvTasa);
        venta.setIgv(igv);
        venta.setTotal(total);
        venta.setEstado(Venta.EstadoVenta.PENDIENTE);
        venta.setFechaHora(LocalDateTime.now());
        if (venta.getNumeroPedido() == null || venta.getNumeroPedido().isBlank()) {
            venta.setNumeroPedido(generarNumeroPedido());
        }

        // ---- 3. Guardar la venta con sus detalles ----
        venta.getDetalles().clear();
        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(venta);
            venta.getDetalles().add(detalle);
        }
        Venta guardada = ventaRepository.save(venta);

        // ---- 4. Descontar stock de los insumos y registrar en el kardex ----
        descontarInventario(guardada, detalles);

        return ventaRepository.save(guardada);
    }

    /**
     * Recorre las recetas de cada bebida vendida y descuenta el stock de los
     * insumos. Si un mismo insumo aparece en varias bebidas de la misma venta
     * se acumula el descuento para generar un solo movimiento.
     */
    private void descontarInventario(Venta venta, List<DetalleVenta> detalles) {

        Map<Long, BigDecimal> descuentoPorProducto = new LinkedHashMap<>();
        Map<Long, Producto> productos = new LinkedHashMap<>();

        for (DetalleVenta detalle : detalles) {
            List<Receta> recetas = recetaRepository.findByBebidaId(detalle.getBebida().getId());

            for (Receta receta : recetas) {
                Producto producto = receta.getProducto();
                if (producto == null || producto.getId() == null) {
                    continue;
                }

                // La cantidad de la receta ya esta en la unidad del producto
                // (producto.unidad_medida), asi que no hay nada que convertir:
                // multiplicar por cuantos vasos de esa bebida se vendieron.
                BigDecimal aDescontar = receta.getCantidad()
                        .multiply(BigDecimal.valueOf(detalle.getCantidad()));

                descuentoPorProducto.merge(producto.getId(), aDescontar, BigDecimal::add);
                productos.put(producto.getId(), producto);
            }
        }

        for (Map.Entry<Long, BigDecimal> entry : descuentoPorProducto.entrySet()) {

            Producto producto = productos.get(entry.getKey());
            BigDecimal aDescontar = entry.getValue();

            BigDecimal stockAnterior = producto.getStockActual() != null
                    ? producto.getStockActual()
                    : BigDecimal.ZERO;
            BigDecimal stockNuevo = stockAnterior.subtract(aDescontar);

            // No dejamos que el stock quede en negativo
            if (stockNuevo.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        "Stock insuficiente de '" + producto.getNombre() + "'. Disponible: "
                                + stockAnterior.stripTrailingZeros().toPlainString()
                                + ", requerido: " + aDescontar.stripTrailingZeros().toPlainString());
            }

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setTipoMovimiento(MovimientoInventario.TipoMovimiento.SALIDA);
            movimiento.setCantidad(aDescontar);
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockNuevo(stockNuevo);
            movimiento.setMotivo("Venta " + venta.getNumeroPedido());
            movimiento.setTipoReferencia(MovimientoInventario.TipoReferencia.VENTA);
            movimiento.setReferenciaId(venta.getId());
            movimiento.setFechaMovimiento(LocalDateTime.now());
            movimiento.setUsuario(venta.getUsuario());
            movimientoRepository.save(movimiento);

            producto.setStockActual(stockNuevo);
            productoRepository.save(producto);
        }
    }

    /**
     * Lee la tasa de IGV vigente del parametro IGV. Si no existe o esta
     * mal escrito se usa la de defecto, para que una venta nunca falle
     * por un dato de configuracion.
     */
    private BigDecimal obtenerTasaIgv() {
        return parametroRepository.findByCodigo(PARAMETRO_IGV)
                .map(Parametro::getValor)
                .map(VentaService::interpretarDecimal)
                .orElse(IGV_POR_DEFECTO);
    }

    private static BigDecimal interpretarDecimal(String valor) {
        try {
            return new BigDecimal(valor.trim());
        } catch (RuntimeException e) {
            return IGV_POR_DEFECTO;
        }
    }

    /**
     * Numero de pedido legible para el cliente: PED-YYYYMMDD-####.
     * La secuencia se reinicia cada dia y se deriva del ultimo pedido
     * emitido hoy (la columna es UNIQUE: si dos empleados chocan en el
     * mismo instante, el segundo transaccion falla y se reintenta).
     */
    private String generarNumeroPedido() {
        String prefijo = "PED-" + LocalDate.now().format(FORMATO_FECHA_PEDIDO) + "-";
        List<String> ultimos = ventaRepository.findUltimosNumeroPedido(prefijo + "%", PageRequest.of(0, 1));
        String ultimoDeHoy = ultimos.isEmpty() ? null : ultimos.get(0);

        int secuencia = 1;
        if (ultimoDeHoy != null && ultimoDeHoy.length() > prefijo.length()) {
            try {
                secuencia = Integer.parseInt(ultimoDeHoy.substring(prefijo.length())) + 1;
            } catch (NumberFormatException ignorado) {
                // El ultimo pedido no termina en numeros: se reinicia en 1.
            }
        }
        return String.format("%s%04d", prefijo, secuencia);
    }
}
