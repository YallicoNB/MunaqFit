package com.munaqfit.backend.controller.Admin;

import com.munaqfit.backend.model.Proveedor;
import com.munaqfit.backend.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/proveedores")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProveedorController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @GetMapping
    public ResponseEntity<List<Proveedor>> listarProveedores() {
        return ResponseEntity.ok(proveedorRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Proveedor> crearProveedor(@RequestBody Proveedor proveedor) {
        return ResponseEntity.ok(proveedorRepository.save(proveedor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> actualizarProveedor(@PathVariable Long id, @RequestBody Proveedor proveedorDetalles) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        
        proveedor.setNombre(proveedorDetalles.getNombre());
        proveedor.setRuc(proveedorDetalles.getRuc());
        proveedor.setTelefono(proveedorDetalles.getTelefono());
        proveedor.setDireccion(proveedorDetalles.getDireccion());
        proveedor.setContactoNombre(proveedorDetalles.getContactoNombre());
        proveedor.setEstado(proveedorDetalles.getEstado());
        
        return ResponseEntity.ok(proveedorRepository.save(proveedor));
    }

    // Elimina un proveedor. Si tiene productos o pagos asociados se da de
    // baja logica (estado = INACTIVO) para no romper las referencias.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProveedor(@PathVariable Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        if (proveedor.getProductos() != null && !proveedor.getProductos().isEmpty()) {
            proveedor.setEstado(Proveedor.EstadoContrato.INACTIVO);
            proveedorRepository.save(proveedor);
            return ResponseEntity.ok("El proveedor tiene productos o pagos asociados, "
                    + "se dio de baja logicamente (estado INACTIVO)");
        }

        proveedorRepository.delete(proveedor);
        return ResponseEntity.ok("Proveedor eliminado correctamente");
    }
}