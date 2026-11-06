package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.dto.PageResponse;
import com.munaqfit.backend.dto.VentaResumenDTO;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.repository.VentaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/ventas")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Ventas", description = "Bandeja de ordenes con filtros (REQ-014/REQ-015)")
public class AdminVentaController {

    @Autowired
    private VentaRepository ventaRepository;

    // REQ-015: Ver todas las ventas; REQ-014: filtros fecha/empleado/estado (tarea 2.6)
    @Operation(summary = "Lista ventas paginadas filtrando por fecha, empleado y estado")
    @GetMapping
    public ResponseEntity<PageResponse<VentaResumenDTO>> listarTodasLasVentas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) Venta.EstadoVenta estado) {

        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 100));

        LocalDateTime inicio = fecha != null ? fecha.atStartOfDay() : null;
        LocalDateTime fin = fecha != null ? fecha.plusDays(1).atStartOfDay() : null;

        Page<Venta> page = ventaRepository.buscarAdmin(inicio, fin, empleadoId, estado, pageable);
        return ResponseEntity.ok(PageResponse.desde(page, VentaResumenDTO::desde));
    }

    // Cambiar estado de una venta (PENDIENTE, PAGADO, CANCELADO)
    @Operation(summary = "Cambia el estado de una venta")
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoVenta(@PathVariable Long id,
                                                   @RequestParam Venta.EstadoVenta nuevoEstado) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        venta.setEstado(nuevoEstado);
        ventaRepository.save(venta);

        return ResponseEntity.ok("Estado de la venta actualizado a: " + nuevoEstado);
    }
}
