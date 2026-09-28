package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.ReabastecimientoRequest;
import com.munaqfit.backend.model.MovimientoInventario;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.MovimientoInventarioRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // REQ-019: Ver stock actual
    public List<Producto> obtenerTodoElInventario() {
        return productoRepository.findAll();
    }

    // REQ-021: Alertas de stock critico
    public List<Producto> obtenerInsumosEnStockCritico() {
        return productoRepository.findStockCritico();
    }

    // REQ-031: Alertas de stock bajo
    public List<Producto> obtenerInsumosEnStockBajo() {
        return productoRepository.findStockBajo();
    }

    // REQ-020 y REQ-022: Agregar stock y registrar en Kardex
    @Transactional
    public Producto registrarReabastecimiento(ReabastecimientoRequest request, Long adminId) {
        
        // Validar que el producto y el administrador existan
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + request.getProductoId()));

        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con ID: " + adminId));

        // Calcular el nuevo stock
        BigDecimal stockAnterior = producto.getStockActual();
        BigDecimal nuevoStock = stockAnterior.add(request.getCantidad());

        // Crear el registro para el movimiento de Inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        
        // Asignacion del Enum interno
        movimiento.setTipoMovimiento(MovimientoInventario.TipoMovimiento.INGRESO); 
        
        movimiento.setCantidad(request.getCantidad());
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(nuevoStock);
        movimiento.setMotivo(request.getMotivo() != null ? request.getMotivo() : "Reabastecimiento de administrador");
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setUsuario(admin);

        // Guardar el movimiento en el historial
        movimientoRepository.save(movimiento);

        // Actualizar los datos del producto
        producto.setStockActual(nuevoStock);
        if (request.getFechaCaducidad() != null) {
            producto.setFechaCaducidad(request.getFechaCaducidad());
        }

        // Guardar y retornar el producto actualizado
        return productoRepository.save(producto);
    }
}