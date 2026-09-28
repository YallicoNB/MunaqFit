package com.munaqfit.backend.dto;

import java.math.BigDecimal;

public class RankingBebidaDTO {
    private Integer posicion;
    private Long bebidaId;
    private String nombreBebida;
    private Long cantidadVendida;
    private BigDecimal totalRecaudado;
    private Double porcentajeTotal;

    public RankingBebidaDTO() {}

    public RankingBebidaDTO(Long bebidaId, String nombreBebida, Long cantidadVendida, BigDecimal totalRecaudado) {
        this.bebidaId = bebidaId;
        this.nombreBebida = nombreBebida;
        this.cantidadVendida = cantidadVendida;
        this.totalRecaudado = totalRecaudado;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Long getBebidaId() {
        return bebidaId;
    }

    public void setBebidaId(Long bebidaId) {
        this.bebidaId = bebidaId;
    }

    public String getNombreBebida() {
        return nombreBebida;
    }

    public void setNombreBebida(String nombreBebida) {
        this.nombreBebida = nombreBebida;
    }

    public Long getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(Long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getTotalRecaudado() {
        return totalRecaudado;
    }

    public void setTotalRecaudado(BigDecimal totalRecaudado) {
        this.totalRecaudado = totalRecaudado;
    }

    public Double getPorcentajeTotal() {
        return porcentajeTotal;
    }

    public void setPorcentajeTotal(Double porcentajeTotal) {
        this.porcentajeTotal = porcentajeTotal;
    }
}