package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.ProductoRequestDTO;
import com.munaqfit.backend.model.Categoria;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.ProductoProveedor;
import com.munaqfit.backend.model.Proveedor;
import com.munaqfit.backend.repository.CategoriaRepository;
import com.munaqfit.backend.repository.ProductoProveedorRepository;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoProveedorRepository productoProveedorRepository;

    public ProductoService(ProductoRepository productoRepository, 
                           CategoriaRepository categoriaRepository,
                           ProveedorRepository proveedorRepository,
                           ProductoProveedorRepository productoProveedorRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoProveedorRepository = productoProveedorRepository;
    }

    @Transactional
    public Producto crearProducto(ProductoRequestDTO request) {
        Producto producto = new Producto();
        
        producto.setNombre(request.getNombre());
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setStockCritico(request.getStockCritico());
        producto.setCostoUnitario(request.getCostoUnitario());
        producto.setFechaCaducidad(request.getFechaCaducidad());
        producto.setUltimaReposicion(request.getUltimaReposicion());
        
        // Conversión limpia del Enum leyendo directamente desde la clase Producto
        if (request.getUnidadMedida() != null && !request.getUnidadMedida().isEmpty()) {
            producto.setUnidadMedida(Producto.UnidadMedida.valueOf(request.getUnidadMedida().toUpperCase()));
        }

        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }

        Producto productoGuardado = productoRepository.save(producto);

        // Guardado completo de la tabla puente (con precio y esPrincipal)
        if (request.getProveedores() != null) {
            for (ProductoRequestDTO.ProveedorAsignadoDTO provReq : request.getProveedores()) {
                Proveedor proveedor = proveedorRepository.findById(provReq.getProveedorId())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + provReq.getProveedorId()));
                
                ProductoProveedor relacion = new ProductoProveedor();
                relacion.setProducto(productoGuardado);
                relacion.setProveedor(proveedor);
                relacion.setPrecioUnitario(provReq.getPrecioUnitario());
                relacion.setEsPrincipal(provReq.getEsPrincipal() != null ? provReq.getEsPrincipal() : false);
                
                productoProveedorRepository.save(relacion);
            }
        }

        return productoGuardado;
    }

    @Transactional
    public Producto actualizarProducto(Long id, ProductoRequestDTO request) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
        producto.setNombre(request.getNombre());
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setStockCritico(request.getStockCritico());
        producto.setCostoUnitario(request.getCostoUnitario());
        producto.setFechaCaducidad(request.getFechaCaducidad());
        producto.setUltimaReposicion(request.getUltimaReposicion());

        // Conversión limpia del Enum leyendo directamente desde la clase Producto
        if (request.getUnidadMedida() != null && !request.getUnidadMedida().isEmpty()) {
            producto.setUnidadMedida(Producto.UnidadMedida.valueOf(request.getUnidadMedida().toUpperCase()));
        }

        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }

        Producto productoActualizado = productoRepository.save(producto);

        productoProveedorRepository.deleteAll(producto.getProductoProveedores());
        
        // Guardado completo de las nuevas relaciones
        if (request.getProveedores() != null) {
            for (ProductoRequestDTO.ProveedorAsignadoDTO provReq : request.getProveedores()) {
                Proveedor proveedor = proveedorRepository.findById(provReq.getProveedorId())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + provReq.getProveedorId()));
                
                ProductoProveedor relacion = new ProductoProveedor();
                relacion.setProducto(productoActualizado);
                relacion.setProveedor(proveedor);
                relacion.setPrecioUnitario(provReq.getPrecioUnitario());
                relacion.setEsPrincipal(provReq.getEsPrincipal() != null ? provReq.getEsPrincipal() : false);
                
                productoProveedorRepository.save(relacion);
            }
        }

        return productoActualizado;
    }

    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
        productoProveedorRepository.deleteAll(producto.getProductoProveedores());
        productoRepository.deleteById(id);
    }
}