package com.munaqfit.backend.dto;

import java.math.BigDecimal;

public class DashboardMetricsDTO {
    private BigDecimal ventasHoy;
    private Long totalClientes;
    private Long productosActivos;
    private Long alertasStockCritico;

    public DashboardMetricsDTO() {}

    public DashboardMetricsDTO(BigDecimal ventasHoy, Long totalClientes, Long productosActivos, Long alertasStockCritico) {
        this.ventasHoy = ventasHoy;
        this.totalClientes = totalClientes;
        this.productosActivos = productosActivos;
        this.alertasStockCritico = alertasStockCritico;
    }

    public BigDecimal getVentasHoy() {
        return ventasHoy;
    }

    public void setVentasHoy(BigDecimal ventasHoy) {
        this.ventasHoy = ventasHoy;
    }

    public Long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(Long totalClientes) {
        this.totalClientes = totalClientes;
    }

    public Long getProductosActivos() {
        return productosActivos;
    }

    public void setProductosActivos(Long productosActivos) {
        this.productosActivos = productosActivos;
    }

    public Long getAlertasStockCritico() {
        return alertasStockCritico;
    }

    public void setAlertasStockCritico(Long alertasStockCritico) {
        this.alertasStockCritico = alertasStockCritico;
    }
}