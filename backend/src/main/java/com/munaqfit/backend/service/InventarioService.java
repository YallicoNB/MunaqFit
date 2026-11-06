package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.InventarioProductoDTO;
import com.munaqfit.backend.dto.PageResponse;
import com.munaqfit.backend.dto.ReabastecimientoRequest;
import com.munaqfit.backend.model.MovimientoInventario;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.ProductoProveedor;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.MovimientoInventarioRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.UsuarioRepository;
import com.munaqfit.backend.util.Sanitizacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventarioService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlertaSseService alertaSseService;

    // REQ-019: Ver stock actual
    public List<Producto> obtenerTodoElInventario() {
        return productoRepository.findAll();
    }

    /**
     * Inventario paginado con los datos de REQ-019/REQ-020 (tareas 2.5 y 2.7):
     * categoria, unidad de medida, ultima reposicion, caducidad y proveedores.
     */
    @Transactional(readOnly = true)
    public PageResponse<InventarioProductoDTO> listarInventarioPaginado(int pagina,
                                                                        int tamano,
                                                                        String busqueda,
                                                                        Long categoriaId,
                                                                        boolean soloBajo) {
        Pageable pageable = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamano, 1), 100),
                Sort.by(Sort.Direction.ASC, "nombre"));

        Page<Producto> page = productoRepository.buscarInventario(
                Sanitizacion.texto(busqueda), categoriaId, soloBajo, pageable);
        return PageResponse.desde(page, this::aInventarioDto);
    }

    private InventarioProductoDTO aInventarioDto(Producto p) {
        InventarioProductoDTO dto = new InventarioProductoDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setStockActual(p.getStockActual());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setStockCritico(p.getStockCritico());
        dto.setCostoUnitario(p.getCostoUnitario());
        dto.setFechaCaducidad(p.getFechaCaducidad());
        dto.setUltimaReposicion(p.getUltimaReposicion());
        dto.setUnidadMedida(p.getUnidadMedida() != null ? p.getUnidadMedida().name() : null);

        if (p.getCategoria() != null) {
            dto.setCategoriaId(p.getCategoria().getId());
            dto.setCategoria(p.getCategoria().getNombre());
        }

        BigDecimal actual = p.getStockActual() != null ? p.getStockActual() : BigDecimal.ZERO;
        BigDecimal critico = p.getStockCritico() != null ? p.getStockCritico() : BigDecimal.ZERO;
        BigDecimal minimo = p.getStockMinimo() != null ? p.getStockMinimo() : BigDecimal.ZERO;

        if (actual.compareTo(critico) <= 0) {
            dto.setEstadoStock("CRITICO");
        } else if (actual.compareTo(minimo) <= 0) {
            dto.setEstadoStock("BAJO");
        } else {
            dto.setEstadoStock("OK");
        }

        List<InventarioProductoDTO.ProveedorInfo> proveedores = new ArrayList<>();
        if (p.getProductoProveedores() != null) {
            for (ProductoProveedor pp : p.getProductoProveedores()) {
                if (pp.getProveedor() == null) {
                    continue;
                }
                proveedores.add(new InventarioProductoDTO.ProveedorInfo(
                        pp.getProveedor().getId(),
                        pp.getProveedor().getNombre(),
                        pp.getPrecioUnitario(),
                        Boolean.TRUE.equals(pp.getEsPrincipal())));
            }
        }
        dto.setProveedores(proveedores);
        return dto;
    }

    // REQ-021: Alertas de stock critico
    public List<Producto> obtenerInsumosEnStockCritico() {
        return productoRepository.findStockCritico();
    }

    // REQ-031: Alertas de stock bajo
    public List<Producto> obtenerInsumosEnStockBajo() {
        return productoRepository.findStockBajo();
    }

    // REQ-020 y REQ-022: Agregar stock y registrar en Kardex
    @Transactional
    public Producto registrarReabastecimiento(ReabastecimientoRequest request, Long adminId) {
        
        // Validar que el producto y el administrador existan
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + request.getProductoId()));

        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con ID: " + adminId));

        // Calcular el nuevo stock
        BigDecimal stockAnterior = producto.getStockActual();
        BigDecimal nuevoStock = stockAnterior.add(request.getCantidad());

        // Crear el registro para el movimiento de Inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        
        // Asignacion del Enum interno
        movimiento.setTipoMovimiento(MovimientoInventario.TipoMovimiento.INGRESO); 
        
        movimiento.setCantidad(request.getCantidad());
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(nuevoStock);
        String motivo = Sanitizacion.texto(request.getMotivo());
        movimiento.setMotivo(motivo != null && !motivo.isBlank()
                ? motivo
                : "Reabastecimiento de administrador");
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setUsuario(admin);

        // Guardar el movimiento en el historial
        movimientoRepository.save(movimiento);

        // Actualizar los datos del producto
        producto.setStockActual(nuevoStock);
        producto.setUltimaReposicion(LocalDateTime.now());
        if (request.getFechaCaducidad() != null) {
            producto.setFechaCaducidad(request.getFechaCaducidad());
        }

        // Guardar y retornar el producto actualizado
        Producto guardado = productoRepository.save(producto);

        // REQ-031 (reparto 3, P1): si el reabastecimiento saco al insumo del
        // stock critico, se avisa en tiempo real para que el equipo sepa que
        // ya esta disponible otra vez.
        BigDecimal stockCritico = guardado.getStockCritico();
        if (stockCritico != null
                && stockAnterior.compareTo(stockCritico) <= 0
                && nuevoStock.compareTo(stockCritico) > 0) {
            alertaSseService.notificarTrasCommit(
                    () -> alertaSseService.notificarStockRecuperado(guardado, nuevoStock));
        }

        return guardado;
    }
}