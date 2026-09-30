package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.dto.KardexFilaDTO;
import com.munaqfit.backend.model.MovimientoInventario.TipoMovimiento;
import com.munaqfit.backend.service.ReporteKardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reportes")
@CrossOrigin(origins = "*")
public class ReporteKardexController {

    @Autowired
    private ReporteKardexService kardexService;

    // Tarea 2.4: Endpoint JSON normal
    @GetMapping("/kardex")
    public ResponseEntity<List<KardexFilaDTO>> obtenerKardex(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long usuarioId) {
        
        List<KardexFilaDTO> reporte = kardexService.obtenerReporteKardex(fechaDesde, fechaHasta, productoId, tipo, usuarioId);
        return ResponseEntity.ok(reporte);
    }

    // Tarea 2.5: Endpoint CSV
    @GetMapping("/kardex/csv")
    public ResponseEntity<byte[]> descargarKardexCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long usuarioId) {

        List<KardexFilaDTO> reporte = kardexService.obtenerReporteKardex(fechaDesde, fechaHasta, productoId, tipo, usuarioId);
        
        StringBuilder csvContent = new StringBuilder();
        // Cabeceras con separador ;
        csvContent.append("Fecha/Hora;Insumo;Tipo;Cantidad;Stock Anterior;Stock Nuevo;Motivo;Usuario\n");
        
        for (KardexFilaDTO fila : reporte) {
            csvContent.append(fila.fechaHora() != null ? fila.fechaHora().toString() : "").append(";")
                      .append(fila.insumo() != null ? fila.insumo() : "").append(";")
                      .append(fila.tipo() != null ? fila.tipo() : "").append(";")
                      .append(fila.cantidad() != null ? fila.cantidad() : 0).append(";")
                      .append(fila.stockAnterior() != null ? fila.stockAnterior() : 0).append(";")
                      .append(fila.stockNuevo() != null ? fila.stockNuevo() : 0).append(";")
                      .append(fila.motivo() != null ? fila.motivo() : "").append(";")
                      .append(fila.usuario() != null ? fila.usuario() : "").append("\n");
        }

        // BOM UTF-8 para que Excel reconozca los acentos
        byte[] csvBytes = ("\uFEFF" + csvContent.toString()).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=kardex_reporte.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}