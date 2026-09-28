package com.munaqfit.backend.controller;

import com.munaqfit.backend.model.ClienteFidelidad;
import com.munaqfit.backend.service.FidelidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/empleado/fidelidad")
@CrossOrigin(origins = "*")
public class EmpleadoFidelidadController {

    @Autowired
    private FidelidadService fidelidadService;

    //Se inscribe a un cliente frecuente
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarClienteFrecuente(@RequestBody ClienteFidelidad cliente) {
        try {
            ClienteFidelidad nuevoCliente = fidelidadService.registrarCliente(cliente);
            return ResponseEntity.ok(nuevoCliente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar cliente: " + e.getMessage());
        }
    }

    // Se suma una visita a un cliente existente y comprobar si toca premio
    @PostMapping("/{clienteId}/visita")
    public ResponseEntity<?> registrarVisita(@PathVariable Long clienteId) {
        try {
            String mensajeResultado = fidelidadService.registrarVisita(clienteId);
            return ResponseEntity.ok(Map.of("mensaje", mensajeResultado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}