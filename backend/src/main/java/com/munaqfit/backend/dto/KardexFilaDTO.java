package com.munaqfit.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Movimiento de inventario para el reporte Kardex.")
public record KardexFilaDTO(
    LocalDateTime fechaHora,
    String insumo,
    String tipo,
    BigDecimal cantidad,
    BigDecimal stockAnterior,
    BigDecimal stockNuevo,
    String motivo,
    String usuario
) {}