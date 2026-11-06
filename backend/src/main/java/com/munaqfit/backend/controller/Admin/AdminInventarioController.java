package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.dto.InventarioProductoDTO;
import com.munaqfit.backend.dto.PageResponse;
import com.munaqfit.backend.dto.ReabastecimientoRequest;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.UsuarioRepository;
import com.munaqfit.backend.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventario")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Inventario", description = "Stock, reabastecimiento y alertas")
public class AdminInventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Lista el inventario paginado con categoria, unidad, caducidad y proveedores")
    @GetMapping
    public ResponseEntity<PageResponse<InventarioProductoDTO>> obtenerInventario(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "false") boolean soloBajo) {
        return ResponseEntity.ok(inventarioService.listarInventarioPaginado(
                pagina, tamano, busqueda, categoriaId, soloBajo));
    }

    @Operation(summary = "Lista los insumos en stock critico")
    @GetMapping("/critico")
    public ResponseEntity<List<Producto>> obtenerStockCritico() {
        return ResponseEntity.ok(inventarioService.obtenerInsumosEnStockCritico());
    }

    @Operation(summary = "Lista los insumos en stock bajo")
    @GetMapping("/bajo")
    public ResponseEntity<List<Producto>> obtenerStockBajo() {
        return ResponseEntity.ok(inventarioService.obtenerInsumosEnStockBajo());
    }

    @Operation(summary = "Registra un reabastecimiento y actualiza el stock + kardex")
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