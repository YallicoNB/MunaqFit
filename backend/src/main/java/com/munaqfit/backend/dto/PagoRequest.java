package com.munaqfit.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Datos que envia el empleado al cobrar una venta.
 * Reemplaza al Map<String, BigDecimal> que no permitia mandar el tipo de pago.
 */
@Data
public class PagoRequest {

    private Long ventaId;

    /** Efectivo entregado por el cliente. */
    private BigDecimal montoPagado;

    /** EFECTIVO, YAPE, PLIN, TRANSFERENCIA o QR. Si viene null se asume EFECTIVO. */
    private String tipoPago;

    /** Numero de operacion para pagos digitales (Yape/Plin/transferencia). */
    private String numeroOperacion;

    /** Banco cuando el pago es por transferencia. */
    private String banco;
}
