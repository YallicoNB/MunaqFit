package com.munaqfit.backend.dto;

import java.time.LocalDateTime;

public record KardexFilaDTO(
    LocalDateTime fechaHora,
    String insumo,
    String tipo,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockNuevo,
    String motivo,
    String usuario
) {}