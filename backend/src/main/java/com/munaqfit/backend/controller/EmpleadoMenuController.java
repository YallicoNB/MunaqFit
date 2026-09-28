package com.munaqfit.backend.controller;

import com.munaqfit.backend.model.Bebida;
import com.munaqfit.backend.model.Receta;
import com.munaqfit.backend.repository.BebidaRepository;
import com.munaqfit.backend.repository.RecetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        Optional<Receta> receta = recetaRepository.findById(bebidaId); 
        if (receta.isPresent()) {
            return ResponseEntity.ok(receta.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}