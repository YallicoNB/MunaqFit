package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.dto.ReabastecimientoRequest;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.UsuarioRepository;
import com.munaqfit.backend.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventario")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
    public ResponseEntity<?> reabastecer(@RequestBody ReabastecimientoRequest request, Authentication authentication) {
        // 1. Obtenemos el DNI del token JWT
        String dniAdmin = authentication.getName(); 
        
        // 2. Buscamos al usuario en la BD para sacar su ID real
        Usuario admin = usuarioRepository.findByDni(dniAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado en la base de datos"));
        
        // 3. Ejecutamos la lógica con el ID real
        inventarioService.registrarReabastecimiento(request, admin.getId());
        
        // Retornamos solo un mensaje para evitar el LazyInitializationException que vimos antes
        return ResponseEntity.ok("Stock actualizado exitosamente");
    }
}