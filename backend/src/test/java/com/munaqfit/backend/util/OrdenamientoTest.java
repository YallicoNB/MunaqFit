package com.munaqfit.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrdenamientoTest {

    private static final Set<String> CAMPOS = Set.of("nombre", "fecha");

    @Test
    void parseaCampoYDireccion() {
        Sort sort = Ordenamiento.parsear("nombre,asc", CAMPOS, "fecha");

        assertThat(sort).hasSize(1);
        assertThat(sort.getOrderFor("nombre")).isNotNull();
        assertThat(sort.getOrderFor("nombre").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void usaDescPorDefecto() {
        Sort sort = Ordenamiento.parsear("fecha", CAMPOS, "fecha");

        assertThat(sort.getOrderFor("fecha").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void campoNoPermitidoUsaElDefault() {
        Sort sort = Ordenamiento.parsear("password,asc", CAMPOS, "fecha");

        assertThat(sort.getOrderFor("fecha")).isNotNull();
        assertThat(sort.getOrderFor("password")).isNull();
    }

    @Test
    void ordenVacioUsaElDefaultConLaDireccionIndicada() {
        Sort sort = Ordenamiento.parsear("", CAMPOS, "nombre", Sort.Direction.ASC);

        assertThat(sort.getOrderFor("nombre").getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
