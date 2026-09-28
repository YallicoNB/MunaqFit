package com.munaqfit.backend.controller;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.DetalleVenta;
import com.munaqfit.backend.repository.VentaRepository;
import com.munaqfit.backend.repository.DetalleVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleado/ordenes")
@CrossOrigin(origins = "*")
public class EmpleadoOrdenController {

    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    // 1. Ver todas las órdenes (ventas), de la más reciente a la más antigua
    @GetMapping("/historial")
    public ResponseEntity<List<Venta>> verTodasLasOrdenes() {
        List<Venta> ordenes = ventaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        if (ordenes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ordenes);
    }

    // 2. Ver el detalle de una orden específica (qué productos se vendieron en esa venta)
    @GetMapping("/{ventaId}/detalle")
    public ResponseEntity<?> verDetalleOrden(@PathVariable Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElse(null);

        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
            return ResponseEntity.ok(detalles);
        } catch (Exception e) {
             return ResponseEntity.internalServerError().body("Error al consultar detalles: " + e.getMessage());
        }
    }
}