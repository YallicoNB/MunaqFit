package com.munaqfit.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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