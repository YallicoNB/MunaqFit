package com.munaqfit.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proveedor")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 20, message = "El RUC no puede superar 20 caracteres")
    @Column(length = 20)
    private String ruc;

    @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @Size(max = 200, message = "La direccion no puede superar 200 caracteres")
    @Column(length = 200)
    private String direccion;

    @Size(max = 100, message = "El contacto no puede superar 100 caracteres")
    @Column(name = "contacto_nombre", length = 100)
    private String contactoNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoContrato estado = EstadoContrato.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", length = 20)
    private TipoContrato tipoContrato;

    @JsonIgnore
    @OneToMany(mappedBy = "proveedor")
    private List<ProductoProveedor> productoProveedores = new ArrayList<>();

    public enum EstadoContrato {
        ACTIVO, INACTIVO
    }

    public enum TipoContrato {
        FIJO, VARIABLE
    }
}
