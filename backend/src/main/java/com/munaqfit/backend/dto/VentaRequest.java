package com.munaqfit.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.DetalleVenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Venta y detalles que envia el POS.")
public class VentaRequest {
    @NotNull(message = "Los datos de la venta son obligatorios")
    @Valid
    private Venta venta;

    @NotEmpty(message = "La venta debe tener al menos un detalle")
    @Valid
    private List<DetalleVenta> detalles;
}