package com.munaqfit.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Solicitud de "Crear Token de sesion" de Izipay (fase 1: sandbox).
 *
 * El token NUNCA se pide desde el navegador: el frontend le manda estos
 * datos a nuestro backend y el backend habla con Izipay con las
 * credenciales del comercio, que jamas llegan al cliente.
 */
@Data
public class IzipayTokenRequest {

    /** Monto total a cobrar en soles (ej. 25.90). */
    private BigDecimal monto;

    /** Numero de pedido de MunaqFit (PED-YYYYMMDD-####), queda como referencia. */
    private String pedido;

    /** Descripcion visible del cobro (ej. "Pedido PED-20261020-0001"). */
    private String descripcion;
}
