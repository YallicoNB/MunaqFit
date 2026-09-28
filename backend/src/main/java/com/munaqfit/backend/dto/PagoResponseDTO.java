package com.munaqfit.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta de un pago registrado: incluye el vuelto calculado
 * y el estado en que quedo la venta.
 */
@Data
public class PagoResponseDTO {

    private Long pagoId;
    private Long ventaId;
    private String numeroPedido;

    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private BigDecimal vuelto;

    private String tipoPago;
    private String numeroOperacion;
    private String banco;
    private String estadoPago;

    /** Estado de la venta tras el pago: PENDIENTE, PAGADO o CANCELADO. */
    private String estadoVenta;

    private LocalDateTime fechaPago;
}
