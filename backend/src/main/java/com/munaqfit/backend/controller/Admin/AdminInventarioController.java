package com.munaqfit.backend.controller;

import com.munaqfit.backend.dto.ReabastecimientoRequest;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventario")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerInventario() {
        return ResponseEntity.ok(inventarioService.obtenerTodoElInventario());
    }

    @GetMapping("/critico")
    public ResponseEntity<List<Producto>> obtenerStockCritico() {
        return ResponseEntity.ok(inventarioService.obtenerInsumosEnStockCritico());
    }

    @GetMapping("/bajo")
    public ResponseEntity<List<Producto>> obtenerStockBajo() {
        return ResponseEntity.ok(inventarioService.obtenerInsumosEnStockBajo());
    }

    @PostMapping("/reabastecer")
    public ResponseEntity<Producto> reabastecer(@RequestBody ReabastecimientoRequest request) {
        /* AVISO: El ID del administrador se extrae del token JWT activo.
           Para asegurar la compilacion y funcione sin interferir con JwtAuthenticationFilter, 
           definiremos temporalmente el adminId = 1L. Posteriormente si se agregara el JWT del
           Administrador
         */
        Long adminId = 1L; 
        Producto productoActualizado = inventarioService.registrarReabastecimiento(request, adminId);
        return ResponseEntity.ok(productoActualizado);
    }
}