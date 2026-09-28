package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.RankingBebidaDTO;
import com.munaqfit.backend.dto.ReporteVentasDTO;
import com.munaqfit.backend.model.Pago;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.Venta.EstadoVenta;
import com.munaqfit.backend.repository.DetalleVentaRepository;
import com.munaqfit.backend.repository.PagoRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    // REQ-029: Ranking de Bebidas Mas Vendidas
    public List<RankingBebidaDTO> obtenerRankingBebidas(LocalDateTime inicio, LocalDateTime fin) {
        // Asigna la posicion iterando sobre la lista ordenada que devuelve la base de datos
        List<RankingBebidaDTO> ranking = detalleVentaRepository.findRankingBebidas(inicio, fin, EstadoVenta.PAGADO);
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setPosicion(i + 1);
        }

        // Cada bebida representa un porcentaje del total de unidades vendidas
        long totalVendidas = ranking.stream()
                .mapToLong(r -> r.getCantidadVendida() != null ? r.getCantidadVendida() : 0L)
                .sum();
        for (RankingBebidaDTO item : ranking) {
            long cantidad = item.getCantidadVendida() != null ? item.getCantidadVendida() : 0L;
            double porcentaje = totalVendidas == 0
                    ? 0d
                    : (cantidad * 100d) / totalVendidas;
            item.setPorcentajeTotal(Math.round(porcentaje * 100d) / 100d);
        }

        return ranking;
    }

    // REQ-032: Valorizacion de Inventario
    public BigDecimal obtenerValorizacionInventario() {
        return productoRepository.calcularValorizacionTotal();
    }

    // REQ-030: Reporte de Ventas Diarias/Semanales/Mensuales
    public ReporteVentasDTO generarReporteVentas(LocalDateTime inicio, LocalDateTime fin) {
        ReporteVentasDTO reporte = new ReporteVentasDTO();

        // Total de ingresos en el rango
        BigDecimal totalIngresos = ventaRepository.sumVentasEnRangoYEstado(inicio, fin, EstadoVenta.PAGADO);
        reporte.setTotalIngresos(totalIngresos != null ? totalIngresos : BigDecimal.ZERO);

        // Total de transacciones pagadas
        List<Venta> ventasEnRango = ventaRepository.findByFechaHoraBetween(inicio, fin);
        long transaccionesPagadas = ventasEnRango.stream()
                .filter(v -> v.getEstado() == EstadoVenta.PAGADO)
                .count();
        reporte.setTotalTransacciones(transaccionesPagadas);

        // Promedio diario
        long dias = ChronoUnit.DAYS.between(inicio.toLocalDate(), fin.toLocalDate());
        if (dias == 0) dias = 1; // Para evitar division por cero si el reporte es del mismo dia
        
        BigDecimal promedio = reporte.getTotalIngresos().divide(BigDecimal.valueOf(dias), 2, RoundingMode.HALF_UP);
        reporte.setPromedioDiario(promedio);

        // Producto mas vendido
        List<RankingBebidaDTO> ranking = obtenerRankingBebidas(inicio, fin);
        if (!ranking.isEmpty()) {
            reporte.setProductoMasVendido(ranking.get(0).getNombreBebida());
        } else {
            reporte.setProductoMasVendido("Sin ventas registradas");
        }

        // Ingresos por metodo de pago
        reporte.setIngresosPorMetodoPago(obtenerIngresosPorMetodoPago(inicio, fin));

        return reporte;
    }

    // REQ-030: Cuanto se cobro por cada metodo de pago en el rango
    private Map<String, BigDecimal> obtenerIngresosPorMetodoPago(LocalDateTime inicio, LocalDateTime fin) {
        Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();

        for (Object[] fila : pagoRepository.ingresosPorMetodoPago(inicio, fin, Pago.EstadoPago.COMPLETADO)) {
            Pago.TipoPago tipo = (Pago.TipoPago) fila[0];
            BigDecimal total = (BigDecimal) fila[1];
            porMetodo.put(tipo.name(), total != null ? total : BigDecimal.ZERO);
        }

        return porMetodo;
    }
}