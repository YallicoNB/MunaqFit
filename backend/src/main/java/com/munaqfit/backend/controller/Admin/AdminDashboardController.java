package com.munaqfit.backend.controller.Admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.munaqfit.backend.dto.DashboardMetricsDTO;
import com.munaqfit.backend.model.Venta.EstadoVenta;
import com.munaqfit.backend.repository.ClienteFidelidadRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Dashboard")
public class AdminDashboardController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteFidelidadRepository clienteFidelidadRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Operation(summary = "Metricas del dashboard: ventas de hoy, clientes, productos y alertas")
    @GetMapping("/metricas")
    public ResponseEntity<DashboardMetricsDTO> obtenerMetricas() {
        // Calcular inicio y fin del dia actual
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(23, 59, 59);

        // Obtener las ventas del dia
        BigDecimal ventasHoy = ventaRepository.sumVentasEnRangoYEstado(inicioDia, finDia, EstadoVenta.PAGADO);
        if (ventasHoy == null) ventasHoy = BigDecimal.ZERO;

        // Obtener conteos directos con metodos de Spring Data JPA
        long totalClientes = clienteFidelidadRepository.count();
        long productosActivos = productoRepository.count();
        long alertasStockCritico = productoRepository.countStockCritico();

        // Armar el DTO de respuesta
        DashboardMetricsDTO metricas = new DashboardMetricsDTO(ventasHoy, totalClientes, productosActivos, alertasStockCritico);
        return ResponseEntity.ok(metricas);
    }
}