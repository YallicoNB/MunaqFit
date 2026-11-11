package com.munaqfit.backend.controller;

import com.munaqfit.backend.dto.ProductoRequestDTO;
import com.munaqfit.backend.dto.ProductoResponseDTO;
import com.munaqfit.backend.model.Producto;
import com.munaqfit.backend.model.ProductoProveedor;
import com.munaqfit.backend.repository.ProductoRepository;
import com.munaqfit.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Rutas de productos (insumos) para el frontend.
 * Requieren token JWT valido y rol ADMIN: un empleado no debe crear ni
 * borrar insumos, aunque si puede consultar el menu y las recetas.
 *
 * Devuelven ProductoResponseDTO, no la entidad: desde la N-N un insumo
 * puede tener varios proveedores y el JSON no debe arrastrar relaciones
 * perezosas ni ciclos.
 */
@RestController
@RequestMapping("/api/productos")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Productos (insumos)", description = "CRUD de insumos; solo ADMIN")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    public ProductoController(ProductoRepository productoRepository, ProductoService productoService) {
        this.productoRepository = productoRepository;
        this.productoService = productoService;
    }

    @Operation(summary = "Lista todos los insumos")
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        return ResponseEntity.ok(
                productoRepository.findAll().stream().map(this::aResponse).toList());
    }

    @Operation(summary = "Lista los insumos de una categoria")
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(
                productoRepository.findByCategoriaId(categoriaId).stream().map(this::aResponse).toList());
    }

    @Operation(summary = "Busca insumos por nombre (coincidencia parcial)")
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscar(@RequestParam("nombre") String nombre) {
        return ResponseEntity.ok(
                productoRepository.findByNombreContainingIgnoreCase(nombre).stream().map(this::aResponse).toList());
    }

    @Operation(summary = "Obtiene un insumo por id")
    @ApiResponse(responseCode = "404", description = "Insumo no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(p -> ResponseEntity.ok(aResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea un insumo")
    @ApiResponse(responseCode = "201", description = "Insumo creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        Producto p = productoService.crearProducto(request);
        return new ResponseEntity<>(aResponse(p), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualiza un insumo existente")
    @ApiResponse(responseCode = "200", description = "Insumo actualizado")
    @ApiResponse(responseCode = "404", description = "Insumo no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(@PathVariable Long id,
                                                                  @Valid @RequestBody ProductoRequestDTO request) {
        Producto p = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(aResponse(p));
    }

    @Operation(summary = "Elimina un insumo")
    @ApiResponse(responseCode = "204", description = "Insumo eliminado")
    @ApiResponse(responseCode = "404", description = "Insumo no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
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
                    pp.getProveedor().getId(), pp.getProveedor().getNombre(),
                    pp.getPrecioUnitario(), Boolean.TRUE.equals(pp.getEsPrincipal())));
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
