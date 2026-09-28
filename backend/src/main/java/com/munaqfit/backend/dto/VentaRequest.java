package com.munaqfit.backend.dto;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.DetalleVenta;
import lombok.Data;
import java.util.List;

@Data
public class VentaRequest {
    private Venta venta;
    private List<DetalleVenta> detalles;
}