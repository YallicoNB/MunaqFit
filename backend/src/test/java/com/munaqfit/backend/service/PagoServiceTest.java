package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.PagoRequest;
import com.munaqfit.backend.dto.PagoResponseDTO;
import com.munaqfit.backend.model.Pago;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.repository.PagoRepository;
import com.munaqfit.backend.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private VentaRepository ventaRepository;

    private PagoService pagoService;
    private Venta venta;

    @BeforeEach
    void setUp() {
        pagoService = new PagoService();
        ReflectionTestUtils.setField(pagoService, "pagoRepository", pagoRepository);
        ReflectionTestUtils.setField(pagoService, "ventaRepository", ventaRepository);

        venta = new Venta();
        venta.setId(1L);
        venta.setNumeroPedido("PED-20261106-0001");
        venta.setTotal(new BigDecimal("18.88"));
        venta.setEstado(Venta.EstadoVenta.PENDIENTE);
    }

    @Test
    void calculaVueltoEnEfectivo() {
        assertThat(pagoService.calcularVuelto(new BigDecimal("18.88"), new BigDecimal("20.00")))
                .isEqualByComparingTo("1.12");
    }

    @Test
    void montoInsuficienteFalla() {
        assertThatThrownBy(() -> pagoService.calcularVuelto(
                new BigDecimal("18.88"), new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registraPagoEnEfectivoConVuelto() {
        PagoRequest request = new PagoRequest();
        request.setVentaId(1L);
        request.setMontoPagado(new BigDecimal("20.00"));
        request.setTipoPago("EFECTIVO");

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        PagoResponseDTO respuesta = pagoService.registrarPago(request);

        assertThat(respuesta.getPagoId()).isEqualTo(99L);
        assertThat(respuesta.getMontoTotal()).isEqualByComparingTo("18.88");
        assertThat(respuesta.getVuelto()).isEqualByComparingTo("1.12");
        assertThat(respuesta.getEstadoVenta()).isEqualTo("PAGADO");
        assertThat(venta.getEstado()).isEqualTo(Venta.EstadoVenta.PAGADO);
    }

    @Test
    void pagoDigitalExigeMontoExacto() {
        PagoRequest request = new PagoRequest();
        request.setVentaId(1L);
        request.setMontoPagado(new BigDecimal("20.00"));
        request.setTipoPago("YAPE");

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThatThrownBy(() -> pagoService.registrarPago(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("monto exacto");
    }

    @Test
    void pagoDigitalExactoNoGeneraVuelto() {
        PagoRequest request = new PagoRequest();
        request.setVentaId(1L);
        request.setMontoPagado(new BigDecimal("18.88"));
        request.setTipoPago("TARJETA");

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        PagoResponseDTO respuesta = pagoService.registrarPago(request);

        assertThat(respuesta.getVuelto()).isEqualByComparingTo("0");
        assertThat(respuesta.getTipoPago()).isEqualTo("TARJETA");
    }

    @Test
    void ventaYaPagadaNoSePuedeCobrarDeNuevo() {
        venta.setEstado(Venta.EstadoVenta.PAGADO);
        PagoRequest request = new PagoRequest();
        request.setVentaId(1L);
        request.setMontoPagado(new BigDecimal("18.88"));
        request.setTipoPago("EFECTIVO");

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThatThrownBy(() -> pagoService.registrarPago(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya fue pagada");
    }
}
