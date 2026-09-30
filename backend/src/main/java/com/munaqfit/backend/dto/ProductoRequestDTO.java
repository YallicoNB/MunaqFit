package com.munaqfit.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProductoRequestDTO {
    
    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private BigDecimal stockCritico;
    private BigDecimal costoUnitario;
    private LocalDate fechaCaducidad;
    private LocalDateTime ultimaReposicion;
    private String unidadMedida; 

    @NotNull(message = "Debe tener al menos un proveedor")
    private List<ProveedorAsignadoDTO> proveedores; 

    // --- GETTERS Y SETTERS ---

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public BigDecimal getStockCritico() { return stockCritico; }
    public void setStockCritico(BigDecimal stockCritico) { this.stockCritico = stockCritico; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    public LocalDateTime getUltimaReposicion() { return ultimaReposicion; }
    public void setUltimaReposicion(LocalDateTime ultimaReposicion) { this.ultimaReposicion = ultimaReposicion; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public List<ProveedorAsignadoDTO> getProveedores() { return proveedores; }
    public void setProveedores(List<ProveedorAsignadoDTO> proveedores) { this.proveedores = proveedores; }

    // --- CLASE ANIDADA ---

    public static class ProveedorAsignadoDTO {
        @NotNull(message = "El ID del proveedor es obligatorio")
        private Long proveedorId;
        
        @NotNull(message = "El precio unitario del proveedor es obligatorio")
        private BigDecimal precioUnitario;
        
        private Boolean esPrincipal;

        public Long getProveedorId() { return proveedorId; }
        public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }
        
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
        
        public Boolean getEsPrincipal() { return esPrincipal; }
        public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }
    }
}