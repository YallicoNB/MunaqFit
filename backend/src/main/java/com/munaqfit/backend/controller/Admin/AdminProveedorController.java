package com.munaqfit.backend.controller.Admin;

import jakarta.validation.Valid;

import com.munaqfit.backend.model.Proveedor;
import com.munaqfit.backend.repository.ProveedorRepository;
import com.munaqfit.backend.util.Sanitizacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/proveedores")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Proveedores", description = "CRUD de proveedores (REQ-026)")
public class AdminProveedorController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Operation(summary = "Lista todos los proveedores")
    @GetMapping
    public ResponseEntity<List<Proveedor>> listarProveedores() {
        return ResponseEntity.ok(proveedorRepository.findAll());
    }

    @Operation(summary = "Crea un proveedor")
    @PostMapping
    public ResponseEntity<Proveedor> crearProveedor(@Valid @RequestBody Proveedor proveedor) {
        proveedor.setNombre(Sanitizacion.texto(proveedor.getNombre()));
        proveedor.setRuc(Sanitizacion.ruc(proveedor.getRuc()));
        proveedor.setTelefono(Sanitizacion.telefono(proveedor.getTelefono()));
        proveedor.setDireccion(Sanitizacion.texto(proveedor.getDireccion()));
        proveedor.setContactoNombre(Sanitizacion.texto(proveedor.getContactoNombre()));
        return ResponseEntity.ok(proveedorRepository.save(proveedor));
    }

    @Operation(summary = "Actualiza un proveedor")
    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> actualizarProveedor(@PathVariable Long id, @Valid @RequestBody Proveedor proveedorDetalles) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        
        proveedor.setNombre(Sanitizacion.texto(proveedorDetalles.getNombre()));
        proveedor.setRuc(Sanitizacion.ruc(proveedorDetalles.getRuc()));
        proveedor.setTelefono(Sanitizacion.telefono(proveedorDetalles.getTelefono()));
        proveedor.setDireccion(Sanitizacion.texto(proveedorDetalles.getDireccion()));
        proveedor.setContactoNombre(Sanitizacion.texto(proveedorDetalles.getContactoNombre()));
        proveedor.setEstado(proveedorDetalles.getEstado());
        
        return ResponseEntity.ok(proveedorRepository.save(proveedor));
    }

    // Elimina un proveedor. Si tiene productos o pagos asociados se da de
    // baja logica (estado = INACTIVO) para no romper las referencias.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProveedor(@PathVariable Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        if (proveedor.getProductoProveedores() != null && !proveedor.getProductoProveedores().isEmpty()) {
            proveedor.setEstado(Proveedor.EstadoContrato.INACTIVO);
            proveedorRepository.save(proveedor);
            return ResponseEntity.ok("El proveedor tiene productos o pagos asociados, "
                    + "se dio de baja logicamente (estado INACTIVO)");
        }

        proveedorRepository.delete(proveedor);
        return ResponseEntity.ok("Proveedor eliminado correctamente");
    }
}