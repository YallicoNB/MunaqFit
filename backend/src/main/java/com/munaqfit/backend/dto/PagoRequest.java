package com.munaqfit.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Datos que envia el empleado al cobrar una venta.
 * Reemplaza al Map<String, BigDecimal> que no permitia mandar el tipo de pago.
 */
@Data
@Schema(description = "Datos del cobro de una venta.")
public class PagoRequest {

    @NotNull(message = "El ventaId es obligatorio")
    private Long ventaId;

    /** Efectivo entregado por el cliente. */
    @NotNull(message = "El montoPagado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto pagado debe ser mayor a cero")
    private BigDecimal montoPagado;

    /** EFECTIVO, YAPE, PLIN, TRANSFERENCIA, QR o TARJETA (Izipay). Si viene null se asume EFECTIVO. */
    @Pattern(regexp = "EFECTIVO|YAPE|PLIN|TRANSFERENCIA|QR|TARJETA",
            message = "El tipo de pago no es valido")
    private String tipoPago;

    /** Numero de operacion para pagos digitales (Yape/Plin/transferencia). */
    private String numeroOperacion;

    /** Banco cuando el pago es por transferencia. */
    private String banco;
}
