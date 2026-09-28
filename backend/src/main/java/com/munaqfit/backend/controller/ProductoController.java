package com.munaqfit.backend.controller;

import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.repository.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rutas de productos (insumos) para el frontend.
 * Requieren token JWT valido.
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
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoRepository.findAll());
    }

    /** Lista los productos de una categoria. */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(productoRepository.findByCategoriaId(categoriaId));
    }

    /** Busca productos por nombre (coincidencia parcial). */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscar(@RequestParam("nombre") String nombre) {
        return ResponseEntity.ok(productoRepository.findByNombreContainingIgnoreCase(nombre));
    }

    /** Obtiene un producto por su id. */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
