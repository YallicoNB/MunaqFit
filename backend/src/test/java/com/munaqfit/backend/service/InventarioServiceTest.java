package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.ReabastecimientoRequest;
import com.munaqfit.backend.model.MovimientoInventario;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.MovimientoInventarioRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private MovimientoInventarioRepository movimientoRepository;
    @Mock
    private AlertaSseService alertaSseService;

    private InventarioService inventarioService;

    @BeforeEach
    void setUp() {
        inventarioService = new InventarioService();
        ReflectionTestUtils.setField(inventarioService, "productoRepository", productoRepository);
        ReflectionTestUtils.setField(inventarioService, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(inventarioService, "movimientoRepository", movimientoRepository);
        ReflectionTestUtils.setField(inventarioService, "alertaSseService", alertaSseService);
    }

    @Test
    void reabastecimientoSumaStockYRegistraKardex() {
        Producto producto = new Producto();
        producto.setId(9L);
        producto.setNombre("Avena");
        producto.setStockActual(new BigDecimal("10.000"));
        producto.setStockMinimo(new BigDecimal("5.000"));
        producto.setStockCritico(new BigDecimal("2.000"));

        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setDni("12345678");

        ReabastecimientoRequest request = new ReabastecimientoRequest();
        request.setProductoId(9L);
        request.setCantidad(new BigDecimal("5.000"));
        request.setMotivo("  compra   semanal  ");

        when(productoRepository.findById(9L)).thenReturn(Optional.of(producto));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = inventarioService.registrarReabastecimiento(request, 1L);

        assertThat(resultado.getStockActual()).isEqualByComparingTo("15.000");
        assertThat(resultado.getUltimaReposicion()).isNotNull();

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(captor.capture());
        MovimientoInventario movimiento = captor.getValue();

        assertThat(movimiento.getTipoMovimiento()).isEqualTo(MovimientoInventario.TipoMovimiento.INGRESO);
        assertThat(movimiento.getStockAnterior()).isEqualByComparingTo("10.000");
        assertThat(movimiento.getStockNuevo()).isEqualByComparingTo("15.000");
        assertThat(movimiento.getCantidad()).isEqualByComparingTo("5.000");
        assertThat(movimiento.getMotivo()).isEqualTo("compra semanal");
        assertThat(movimiento.getUsuario()).isEqualTo(admin);
    }

    @Test
    void productoInexistenteFalla() {
        when(productoRepository.findById(404L)).thenReturn(Optional.empty());

        ReabastecimientoRequest request = new ReabastecimientoRequest();
        request.setProductoId(404L);
        request.setCantidad(BigDecimal.ONE);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> inventarioService.registrarReabastecimiento(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Producto no encontrado");
    }
}
