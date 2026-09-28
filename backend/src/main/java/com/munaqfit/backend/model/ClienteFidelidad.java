package com.munaqfit.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cliente_fidelidad")
public class ClienteFidelidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Permite reconocer al cliente que vuelve. Antes no habia forma de
     * saber si "Luis Perez" de la calle A era el mismo de la calle B, y
     * el mismo cliente podia acumular visitas infinitas.
     */
    @Column(length = 20, unique = true)
    private String dni;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private Integer visitas = 0;

    @Column(name = "umbral_premio", nullable = false)
    private Integer umbralPremio = 10;

    @Column(name = "ultima_visita")
    private LocalDateTime ultimaVisita;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @JsonIgnore
    @OneToMany(mappedBy = "clienteFidelidad")
    private List<VisitaCliente> visitasDetalle = new ArrayList<>();
}
