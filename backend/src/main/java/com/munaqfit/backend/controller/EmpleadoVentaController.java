package com.munaqfit.backend.controller;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.service.VentaService;
import com.munaqfit.backend.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empleado/ventas")
// Si se configura CORS globalmente no se necesita esto, pero ayuda para pruebas locales con el frontend
@CrossOrigin(origins = "*") 
public class EmpleadoVentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private PagoService pagoService;

    // Tomar un nuevo pedido
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequest request) {
        try {
            Venta nuevaVenta = ventaService.procesarVenta(request.getVenta(), request.getDetalles());
            return ResponseEntity.ok(nuevaVenta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la venta: " + e.getMessage());
        }
    }

    // Se registra pago y calcular vuelto
    @PostMapping("/pago")
    public ResponseEntity<?> procesarPago(@RequestBody Map<String, BigDecimal> pagoRequest) {
        try {
            BigDecimal total = pagoRequest.get("total");
            BigDecimal recibido = pagoRequest.get("recibido");
            BigDecimal vuelto = pagoService.calcularVuelto(total, recibido);
            
            return ResponseEntity.ok(Map.of("vuelto", vuelto, "mensaje", "Pago registrado con éxito"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}