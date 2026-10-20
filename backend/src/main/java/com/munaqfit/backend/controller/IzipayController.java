package com.munaqfit.backend.controller;

import com.munaqfit.backend.dto.IzipayTokenRequest;
import com.munaqfit.backend.dto.IzipayTokenResponse;
import com.munaqfit.backend.service.IzipayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pasarela de pagos Izipay (reparto 3, Persona 1 - Integraciones).
 *
 * El token de sesion se genera AQUI y no en el navegador: las credenciales
 * del comercio (IZIPAY_API_PASSWORD) nunca se envian al frontend. Cualquier
 * usuario autenticado (empleado o admin) puede pedir el token porque cobrar
 * es una accion de caja, no de administracion.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Pagos", description = "Integracion con la pasarela de pagos Izipay (REQ: cobro con tarjeta)")
public class IzipayController {

    private final IzipayService izipayService;

    public IzipayController(IzipayService izipayService) {
        this.izipayService = izipayService;
    }

    @Operation(summary = "Crea el token de sesion de Izipay para el formulario embebido",
            description = "Se llama desde el backend al pedir un cobro con tarjeta. "
                    + "Devuelve el tokenGUID (authorization), la llave RSA publica, el merchantCode "
                    + "y la URL del SDK. Requiere sesion valida (empleado o admin).")
    @PostMapping("/get-izipay-token")
    public ResponseEntity<IzipayTokenResponse> crearToken(@RequestBody IzipayTokenRequest request) {
        return ResponseEntity.ok(izipayService.crearTokenDeSesion(request));
    }

    @Operation(summary = "Estado de la integracion de pagos",
            description = "true cuando el comercio ya tiene credenciales Izipay configuradas "
                    + "(sandbox o produccion). El frontend lo usa para ocultar o mostrar el metodo TARJETA.")
    @GetMapping("/izipay/configuracion")
    public ResponseEntity<Boolean> configurado() {
        return ResponseEntity.ok(izipayService.estaConfigurado());
    }
}
