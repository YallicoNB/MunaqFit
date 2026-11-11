package com.munaqfit.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.munaqfit.backend.model.Venta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumen de venta para la bandeja administrativa de ordenes (REQ-015).
 * Evita serializar la entidad completa y su relacion perezosa con usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen de venta para la bandeja administrativa.")
public class VentaResumenDTO {

    private Long id;
    private String numeroPedido;
    private LocalDateTime fechaHora;
    private String estado;
    private String tipoVenta;
    private Integer mesa;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String notas;

    private Long empleadoId;
    private String empleado;
    private String dniEmpleado;

    public static VentaResumenDTO desde(Venta venta) {
        VentaResumenDTO dto = new VentaResumenDTO();
        dto.setId(venta.getId());
        dto.setNumeroPedido(venta.getNumeroPedido());
        dto.setFechaHora(venta.getFechaHora());
        dto.setEstado(venta.getEstado() != null ? venta.getEstado().name() : null);
        dto.setTipoVenta(venta.getTipoVenta() != null ? venta.getTipoVenta().name() : null);
        dto.setMesa(venta.getMesa());
        dto.setSubtotal(venta.getSubtotal());
        dto.setIgv(venta.getIgv());
        dto.setTotal(venta.getTotal());
        dto.setNotas(venta.getNotas());

        if (venta.getUsuario() != null) {
            dto.setEmpleadoId(venta.getUsuario().getId());
            dto.setEmpleado(venta.getUsuario().getNombreCompleto());
            dto.setDniEmpleado(venta.getUsuario().getDni());
        }
        return dto;
    }
}
