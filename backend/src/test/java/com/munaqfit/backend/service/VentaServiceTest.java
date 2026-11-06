package com.munaqfit.backend.service;

import com.munaqfit.backend.model.Bebida;
import com.munaqfit.backend.model.DetalleVenta;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.model.Venta;
import com.munaqfit.backend.repository.BebidaRepository;
import com.munaqfit.backend.repository.MovimientoInventarioRepository;
import com.munaqfit.backend.repository.ParametroRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.RecetaRepository;
import com.munaqfit.backend.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private BebidaRepository bebidaRepository;
    @Mock private ParametroRepository parametroRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private RecetaRepository recetaRepository;
    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private AlertaSseService alertaSseService;

    private VentaService ventaService;

    @BeforeEach
    void setUp() {
        ventaService = new VentaService();
        ReflectionTestUtils.setField(ventaService, "ventaRepository", ventaRepository);
        ReflectionTestUtils.setField(ventaService, "bebidaRepository", bebidaRepository);
        ReflectionTestUtils.setField(ventaService, "parametroRepository", parametroRepository);
        ReflectionTestUtils.setField(ventaService, "productoRepository", productoRepository);
        ReflectionTestUtils.setField(ventaService, "recetaRepository", recetaRepository);
        ReflectionTestUtils.setField(ventaService, "movimientoRepository", movimientoRepository);
        ReflectionTestUtils.setField(ventaService, "alertaSseService", alertaSseService);
    }

    @Test
    void usaElPrecioDelCatalogoYNoElQueVengaDelCliente() {
        Bebida brownie = new Bebida();
        brownie.setId(60003L);
        brownie.setNombre("Brownie");
        brownie.setPrecio(new BigDecimal("19.00"));

        Usuario empleado = new Usuario();
        empleado.setId(1L);
        empleado.setDni("12345678");

        Venta venta = new Venta();
        venta.setUsuario(empleado);

        DetalleVenta detalle = new DetalleVenta();
        Bebida bebidaSoloId = new Bebida();
        bebidaSoloId.setId(60003L);
        detalle.setBebida(bebidaSoloId);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(new BigDecimal("1.00")); // intento de manipular el precio

        when(bebidaRepository.findById(60003L)).thenReturn(Optional.of(brownie));
        when(parametroRepository.findByCodigo("IGV")).thenReturn(Optional.empty());
        when(recetaRepository.findByBebidaId(60003L)).thenReturn(List.of());
        when(ventaRepository.findUltimosNumeroPedido(anyString(), any(Pageable.class))).thenReturn(List.of());
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.procesarVenta(venta, List.of(detalle));

        assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo("19.00");
        assertThat(resultado.getSubtotal()).isEqualByComparingTo("19.00");
        assertThat(resultado.getIgv()).isEqualByComparingTo("3.42");
        assertThat(resultado.getTotal()).isEqualByComparingTo("22.42");
        assertThat(resultado.getEstado()).isEqualTo(Venta.EstadoVenta.PENDIENTE);
    }
}
