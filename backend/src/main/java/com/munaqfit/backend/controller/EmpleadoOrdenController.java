package com.munaqfit.backend.controller;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.DetalleVenta;
import com.munaqfit.backend.repository.VentaRepository;
import com.munaqfit.backend.repository.DetalleVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/empleado/ordenes")
@CrossOrigin(origins = "*")
public class EmpleadoOrdenController {

    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    // 1. Ver todas las órdenes (ventas)
    @GetMapping("/historial")
    public ResponseEntity<List<Venta>> verTodasLasOrdenes() {
        List<Venta> ordenes = (List<Venta>) ventaRepository.findAll();
        if (ordenes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ordenes);
    }

    // 2. Ver el detalle de una orden específica (qué productos se vendieron en esa venta)
    @GetMapping("/{ventaId}/detalle")
    public ResponseEntity<?> verDetalleOrden(@PathVariable Long ventaId) {
        Optional<Venta> ventaOptional = ventaRepository.findById(ventaId);
        
        if (!ventaOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        try {
            return ResponseEntity.ok("Endpoint estructurado. Requiere método findByVentaId en el repositorio.");
        } catch (Exception e) {
             return ResponseEntity.internalServerError().body("Error al consultar detalles: " + e.getMessage());
        }
    }
}