package com.munaqfit.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class ProductoRequestDTO {
    
    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    // Aquí recibes los IDs de los proveedores para la relación N-N que hizo el Dev 1
    @NotNull(message = "Debe tener al menos un proveedor")
    private List proveedoresIds; 

    // Agrega los demás campos necesarios (stock, unidadMedida, etc.) y sus Getters/Setters
}