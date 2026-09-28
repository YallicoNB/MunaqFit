package com.munaqfit.backend.controller;

import com.munaqfit.backend.dto.ProductoResponseDTO;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.ProductoProveedor;
import com.munaqfit.backend.repository.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Rutas de productos (insumos) para el frontend.
 * Requieren token JWT valido.
 *
 * Devuelven ProductoResponseDTO, no la entidad: desde la N-N un insumo
 * puede tener varios proveedores y el JSON no debe arrastrar relaciones
 * perezosas ni ciclos.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /** Lista todos los productos. */
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        return ResponseEntity.ok(
                productoRepository.findAll().stream().map(this::aResponse).toList());
    }

    /** Lista los productos de una categoria. */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(
                productoRepository.findByCategoriaId(categoriaId).stream().map(this::aResponse).toList());
    }

    /** Busca productos por nombre (coincidencia parcial). */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscar(@RequestParam("nombre") String nombre) {
        return ResponseEntity.ok(
                productoRepository.findByNombreContainingIgnoreCase(nombre).stream().map(this::aResponse).toList());
    }

    /** Obtiene un producto por su id. */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(p -> ResponseEntity.ok(aResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Convierte la entidad al DTO que consume el frontend. */
    private ProductoResponseDTO aResponse(Producto p) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());

        if (p.getCategoria() != null) {
            dto.setCategoria(new ProductoResponseDTO.CategoriaInfo(
                    p.getCategoria().getId(), p.getCategoria().getNombre()));
        }

        List<ProductoResponseDTO.ProveedorInfo> proveedores = new ArrayList<>();
        for (ProductoProveedor pp : p.getProductoProveedores()) {
            proveedores.add(new ProductoResponseDTO.ProveedorInfo(
                    pp.getProveedor().getId(),
                    pp.getProveedor().getNombre(),
                    pp.getPrecioUnitario(),
                    Boolean.TRUE.equals(pp.getEsPrincipal())));
        }
        dto.setProveedores(proveedores);

        dto.setStockActual(p.getStockActual());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setStockCritico(p.getStockCritico());
        dto.setUnidadMedida(p.getUnidadMedida() != null ? p.getUnidadMedida().name() : null);
        dto.setCostoUnitario(p.getCostoUnitario());
        dto.setFechaCaducidad(p.getFechaCaducidad());
        dto.setUltimaReposicion(p.getUltimaReposicion());
        return dto;
    }
}