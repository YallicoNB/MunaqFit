package com.munaqfit.backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanitizacionTest {

    @Test
    void eliminaBloquesScriptYEtiquetasHtml() {
        assertThat(Sanitizacion.texto("<script>alert('xss')</script>Hola mundo"))
                .isEqualTo("Hola mundo");
        assertThat(Sanitizacion.texto("<b>Texto</b> normal"))
                .isEqualTo("Texto normal");
    }

    @Test
    void recortaYColapsaEspacios() {
        assertThat(Sanitizacion.texto("   Hola    mundo   ")).isEqualTo("Hola mundo");
    }

    @Test
    void nullSigueSiendoNull() {
        assertThat(Sanitizacion.texto(null)).isNull();
        assertThat(Sanitizacion.email(null)).isNull();
        assertThat(Sanitizacion.dni(null)).isNull();
    }

    @Test
    void normalizaEmail() {
        assertThat(Sanitizacion.email("  TEST@Example.COM ")).isEqualTo("test@example.com");
    }

    @Test
    void dejaSoloDigitosEnDni() {
        assertThat(Sanitizacion.dni("12-345 678")).isEqualTo("12345678");
        assertThat(Sanitizacion.ruc("20-123-456-789")).isEqualTo("20123456789");
    }

    @Test
    void conservaFormatoDeTelefono() {
        assertThat(Sanitizacion.telefono("+51 (999) 888-777"))
                .isEqualTo("+51 (999) 888-777");
    }
}
