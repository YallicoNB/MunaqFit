package com.munaqfit.backend.controller;

import com.munaqfit.backend.dto.PagoRequest;
import com.munaqfit.backend.dto.PagoResponseDTO;
import com.munaqfit.backend.dto.VentaRequest;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.repository.UsuarioRepository;
import com.munaqfit.backend.service.VentaService;
import com.munaqfit.backend.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empleado/ventas")
// Si se configura CORS globalmente no se necesita esto, pero ayuda para pruebas locales con el frontend
@CrossOrigin(origins = "*")
public class EmpleadoVentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Tomar un nuevo pedido
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequest request, Authentication authentication) {
        try {
            // La venta siempre la registra el empleado que viene en el token
            Usuario empleado = usuarioRepository.findByDni(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado en la base de datos"));

            if (request.getVenta() == null) {
                request.setVenta(new Venta());
            }
            request.getVenta().setUsuario(empleado);

            Venta nuevaVenta = ventaService.procesarVenta(request.getVenta(), request.getDetalles());
            return ResponseEntity.ok(nuevaVenta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la venta: " + e.getMessage());
        }
    }

    // Se registra el pago, se guarda en la base de datos y la venta pasa a PAGADO
    @PostMapping("/pago")
    public ResponseEntity<?> procesarPago(@RequestBody PagoRequest request) {
        try {
            PagoResponseDTO resultado = pagoService.registrarPago(request);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Solo calcula el vuelto, sin registrar nada (util para el simulacro en el frontend)
    @PostMapping("/vuelto")
    public ResponseEntity<?> calcularVuelto(@RequestBody java.util.Map<String, java.math.BigDecimal> request) {
        try {
            java.math.BigDecimal total = request.get("total");
            java.math.BigDecimal recibido = request.get("recibido");
            if (total == null || recibido == null) {
                return ResponseEntity.badRequest().body("Se deben enviar 'total' y 'recibido'");
            }
            java.math.BigDecimal vuelto = pagoService.calcularVuelto(total, recibido);
            return ResponseEntity.ok(java.util.Map.of("vuelto", vuelto, "mensaje", "Vuelto calculado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
