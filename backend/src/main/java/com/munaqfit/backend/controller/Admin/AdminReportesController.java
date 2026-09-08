package com.munaqfit.backend.controller;

import com.munaqfit.backend.dto.RankingBebidaDTO;
import com.munaqfit.backend.dto.ReporteVentasDTO;
import com.munaqfit.backend.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportesController {

    @Autowired
    private ReporteService reporteService;

    // REQ-029: Obtener ranking de bebidas mas vendidas
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingBebidaDTO>> obtenerRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(reporteService.obtenerRankingBebidas(inicio, fin));
    }

    // REQ-030: Obtener reporte consolidado de ventas
    @GetMapping("/ventas")
    public ResponseEntity<ReporteVentasDTO> obtenerReporteVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(reporteService.generarReporteVentas(inicio, fin));
    }

    // REQ-032: Obtener valorizacion total del inventario
    @GetMapping("/valorizacion")
    public ResponseEntity<BigDecimal> obtenerValorizacion() {
        return ResponseEntity.ok(reporteService.obtenerValorizacionInventario());
    }
}