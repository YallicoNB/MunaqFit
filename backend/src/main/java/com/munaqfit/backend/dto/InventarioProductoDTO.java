package com.munaqfit.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fila de inventario con todos los datos que exige REQ-019/REQ-020:
 * categoria, unidad de medida, ultima reposicion, caducidad y proveedor.
 *
 * <p>No se expone la entidad JPA porque arrastra relaciones perezosas
 * (categoria, productoProveedores, movimientos) y rompe el JSON.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fila de inventario con categoria, unidad, caducidad, ultima reposicion y proveedores.")
public class InventarioProductoDTO {

    private Long id;
    private String nombre;

    private Long categoriaId;
    private String categoria;

    private String unidadMedida;

    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private BigDecimal stockCritico;
    private BigDecimal costoUnitario;

    private LocalDate fechaCaducidad;
    private LocalDateTime ultimaReposicion;

    /** CRITICO cuando stockActual <= stockCritico; BAJO si <= stockMinimo; si no, OK. */
    private String estadoStock;

    private List<ProveedorInfo> proveedores;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProveedorInfo {
        private Long id;
        private String nombre;
        private BigDecimal precioUnitario;
        private Boolean principal;
    }
}
