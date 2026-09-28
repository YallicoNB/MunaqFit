package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ventas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVentaController {

    @Autowired
    private VentaRepository ventaRepository;

    // REQ-015: Ver todas las ventas
    @GetMapping
    public ResponseEntity<List<Venta>> listarTodasLasVentas() {
        return ResponseEntity.ok(ventaRepository.findAllByOrderByFechaHoraDesc());
    }

    // Cambiar estado de una venta (PENDIENTE, PAGADO, CANCELADO)
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoVenta(@PathVariable Long id, @RequestParam Venta.EstadoVenta nuevoEstado) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        
        venta.setEstado(nuevoEstado);
        ventaRepository.save(venta);
        
        return ResponseEntity.ok("Estado de la venta actualizado a: " + nuevoEstado);
    }
}