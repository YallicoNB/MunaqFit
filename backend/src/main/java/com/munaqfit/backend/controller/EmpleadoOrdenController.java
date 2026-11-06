package com.munaqfit.backend.controller;

import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.DetalleVenta;
import com.munaqfit.backend.repository.UsuarioRepository;
import com.munaqfit.backend.repository.VentaRepository;
import com.munaqfit.backend.repository.DetalleVentaRepository;
import com.munaqfit.backend.util.Sanitizacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/empleado/ordenes")
@CrossOrigin(origins = "*")
public class EmpleadoOrdenController {

    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. REQ-014 (tarea 2.6): historial del empleado autenticado, solo de HOY.
    @GetMapping("/historial")
    public ResponseEntity<List<Venta>> verTodasLasOrdenes(Authentication authentication) {
        String dni = null;
        if (authentication != null && authentication.getName() != null) {
            dni = Sanitizacion.dni(authentication.getName());
        }

        Usuario usuario = (dni == null ? null : usuarioRepository.findByDni(dni).orElse(null));
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        LocalDate hoy = LocalDate.now();
        List<Venta> ordenes = ventaRepository.findByUsuarioIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                usuario.getId(),
                hoy.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay());

        // Se devuelve la lista vacia (200) para que la UI muestre su estado
        // "sin ordenes" sin tener que tratar un 204 como caso especial.
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