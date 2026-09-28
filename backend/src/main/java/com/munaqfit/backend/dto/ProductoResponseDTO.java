package com.munaqfit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta de un producto (insumo) para el frontend.
 *
 * No se devuelve la entidad JPA porque desde la N-N un producto deja de
 * tener un solo proveedor: expone la lista completa con su precio y el
 * flag de proveedor principal. Lo consumen las vistas de inventario y el
 * CRUD de productos que desarrolla Dev 4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    private Long id;
    private String nombre;

    /** Categoria abreviada; puede ser null (sin categoria asignada). */
    private CategoriaInfo categoria;

    /** Proveedores del insumo; siempre una lista, puede estar vacia. */
    private List<ProveedorInfo> proveedores;

    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private BigDecimal stockCritico;
    private String unidadMedida;
    private BigDecimal costoUnitario;
    private LocalDate fechaCaducidad;
    private LocalDateTime ultimaReposicion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaInfo {
        private Long id;
        private String nombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProveedorInfo {
        private Long id;
        private String nombre;
        private BigDecimal precioUnitario;
        private boolean esPrincipal;
    }
}