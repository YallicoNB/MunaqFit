package com.munaqfit.backend.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PagoService {

    public BigDecimal calcularVuelto(BigDecimal totalPagar, BigDecimal montoRecibido) {
        if (montoRecibido.compareTo(totalPagar) < 0) {
            throw new IllegalArgumentException("El monto recibido es menor al total a pagar");
        }
        return montoRecibido.subtract(totalPagar);
    }
}