package com.munaqfit.backend.controller;

import com.munaqfit.backend.model.Bebida;
import com.munaqfit.backend.model.Receta;
import com.munaqfit.backend.repository.BebidaRepository;
import com.munaqfit.backend.repository.RecetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleado/menu")
@CrossOrigin(origins = "*") 
public class EmpleadoMenuController {

    @Autowired
    private BebidaRepository bebidaRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    // 1. Ver catálogo de bebidas
    @GetMapping("/bebidas")
    public ResponseEntity<List<Bebida>> obtenerMenuBebidas() {
        List<Bebida> bebidas = (List<Bebida>) bebidaRepository.findAll();
        if (bebidas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bebidas);
    }

    // 2. Consultar recetas de una bebida específica (para preparación)
    @GetMapping("/receta/{bebidaId}")
    public ResponseEntity<?> consultarRecetaPorBebida(@PathVariable Long bebidaId) {
        // Ojo: se busca por bebidaId y no por id, porque una bebida tiene
        // varias recetas (una por insumo) y antes se confundian los dos id.
        List<Receta> recetas = recetaRepository.findByBebidaId(bebidaId);
        if (!recetas.isEmpty()) {
            return ResponseEntity.ok(recetas);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}