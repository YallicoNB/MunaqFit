package com.munaqfit.backend.service;

import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.model.DetalleVenta;
import com.munaqfit.backend.repository.VentaRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public Venta procesarVenta(Venta venta, List<DetalleVenta> detalles) {
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            // Aqui se calcula el subtotal por producto
            BigDecimal subtotal = detalle.getPrecioUnitario().multiply(new BigDecimal(detalle.getCantidad()));
            total = total.add(subtotal);
        }

        venta.setTotal(total);
        // Guardar venta principal
        return ventaRepository.save(venta);
    }
}