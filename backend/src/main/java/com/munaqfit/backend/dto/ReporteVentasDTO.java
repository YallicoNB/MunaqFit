package com.munaqfit.backend.dto;

import java.math.BigDecimal;
import java.util.Map;

public class ReporteVentasDTO {
    private BigDecimal totalIngresos;
    private Long totalTransacciones;
    private BigDecimal promedioDiario;
    private String productoMasVendido;
    private Map<String, BigDecimal> ingresosPorMetodoPago;

    public ReporteVentasDTO() {}

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public Long getTotalTransacciones() {
        return totalTransacciones;
    }

    public void setTotalTransacciones(Long totalTransacciones) {
        this.totalTransacciones = totalTransacciones;
    }

    public BigDecimal getPromedioDiario() {
        return promedioDiario;
    }

    public void setPromedioDiario(BigDecimal promedioDiario) {
        this.promedioDiario = promedioDiario;
    }

    public String getProductoMasVendido() {
        return productoMasVendido;
    }

    public void setProductoMasVendido(String productoMasVendido) {
        this.productoMasVendido = productoMasVendido;
    }

    public Map<String, BigDecimal> getIngresosPorMetodoPago() {
        return ingresosPorMetodoPago;
    }

    public void setIngresosPorMetodoPago(Map<String, BigDecimal> ingresosPorMetodoPago) {
        this.ingresosPorMetodoPago = ingresosPorMetodoPago;
    }
}