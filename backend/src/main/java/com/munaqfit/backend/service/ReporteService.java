package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.RankingBebidaDTO;
import com.munaqfit.backend.dto.ReporteVentasDTO;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.Venta.EstadoVenta;
import com.munaqfit.backend.repository.DetalleVentaRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    // REQ-029: Ranking de Bebidas Mas Vendidas
    public List<RankingBebidaDTO> obtenerRankingBebidas(LocalDateTime inicio, LocalDateTime fin) {
        // Asigna la posicion iterando sobre la lista ordenada que devuelve la base de datos
        List<RankingBebidaDTO> ranking = detalleVentaRepository.findRankingBebidas(inicio, fin, EstadoVenta.PAGADO);
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setPosicion(i + 1);
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

        // Ingresos por metodo de pago (Se inicializa vacio para no romper compilacion; requeriria PagoRepository)
        reporte.setIngresosPorMetodoPago(new HashMap<>());

        return reporte;
    }
}