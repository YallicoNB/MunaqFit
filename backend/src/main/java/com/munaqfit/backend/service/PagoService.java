package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.PagoRequest;
import com.munaqfit.backend.dto.PagoResponseDTO;
import com.munaqfit.backend.model.Pago;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.repository.PagoRepository;
import com.munaqfit.backend.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    public BigDecimal calcularVuelto(BigDecimal totalPagar, BigDecimal montoRecibido) {
        if (montoRecibido.compareTo(totalPagar) < 0) {
            throw new IllegalArgumentException("El monto recibido es menor al total a pagar");
        }
        return montoRecibido.subtract(totalPagar);
    }

    /**
     * Registra el pago de una venta: guarda el registro en la tabla pago,
     * calcula el vuelto y pasa la venta a estado PAGADO.
     */
    @Transactional
    public PagoResponseDTO registrarPago(PagoRequest request) {

        if (request.getVentaId() == null) {
            throw new IllegalArgumentException("El ventaId es obligatorio");
        }
        if (request.getMontoPagado() == null) {
            throw new IllegalArgumentException("El montoPagado es obligatorio");
        }

        Venta venta = ventaRepository.findById(request.getVentaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Venta no encontrada con ID: " + request.getVentaId()));

        if (Venta.EstadoVenta.PAGADO == venta.getEstado()) {
            throw new IllegalStateException(
                    "La venta " + venta.getNumeroPedido() + " ya fue pagada");
        }
        if (Venta.EstadoVenta.CANCELADO == venta.getEstado()) {
            throw new IllegalStateException(
                    "La venta " + venta.getNumeroPedido() + " esta cancelada");
        }

        // Lanza excepcion si el monto es insuficiente
        BigDecimal vuelto = calcularVuelto(venta.getTotal(), request.getMontoPagado());

        Pago.TipoPago tipoPago;
        try {
            tipoPago = (request.getTipoPago() == null || request.getTipoPago().isBlank())
                    ? Pago.TipoPago.EFECTIVO
                    : Pago.TipoPago.valueOf(request.getTipoPago().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Tipo de pago invalido: " + request.getTipoPago()
                            + ". Valores permitidos: EFECTIVO, YAPE, PLIN, TRANSFERENCIA, QR");
        }

        // El vuelto solo existe cuando el pago es en efectivo. Con Yape,
        // Plin, QR o transferencia no hay billetes que devolver, asi que el
        // monto recibido tiene que ser exacto: si sobra, ese sobrante es un
        // error de digito, no un vuelto.
        BigDecimal cambio;
        if (tipoPago == Pago.TipoPago.EFECTIVO) {
            cambio = vuelto;
        } else {
            if (vuelto.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException(
                        "El pago con " + tipoPago + " debe ser por el monto exacto ("
                                + venta.getTotal() + "), no " + request.getMontoPagado()
                                + ". El vuelto solo aplica a pagos en efectivo.");
            }
            cambio = BigDecimal.ZERO;
        }

        Pago pago = new Pago();
        pago.setVenta(venta);
        pago.setMontoPagado(request.getMontoPagado());
        pago.setMontoCambio(cambio);
        pago.setTipoPago(tipoPago);
        pago.setNumeroOperacion(request.getNumeroOperacion());
        pago.setBanco(request.getBanco());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado(Pago.EstadoPago.COMPLETADO);
        Pago guardado = pagoRepository.save(pago);

        // La venta queda pagada
        venta.setEstado(Venta.EstadoVenta.PAGADO);
        ventaRepository.save(venta);

        PagoResponseDTO response = new PagoResponseDTO();
        response.setPagoId(guardado.getId());
        response.setVentaId(venta.getId());
        response.setNumeroPedido(venta.getNumeroPedido());
        // El frontend sigue esperando 'montoTotal' en la respuesta: se
        // toma de la venta, que es donde vive el dato.
        response.setMontoTotal(venta.getTotal());
        response.setMontoPagado(guardado.getMontoPagado());
        response.setVuelto(guardado.getMontoCambio());
        response.setTipoPago(guardado.getTipoPago().name());
        response.setNumeroOperacion(guardado.getNumeroOperacion());
        response.setBanco(guardado.getBanco());
        response.setEstadoPago(guardado.getEstado().name());
        response.setEstadoVenta(venta.getEstado().name());
        response.setFechaPago(guardado.getFechaPago());

        return response;
    }
}
