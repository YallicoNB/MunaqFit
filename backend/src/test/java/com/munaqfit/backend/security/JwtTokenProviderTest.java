package com.munaqfit.backend.security;

import com.munaqfit.backend.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        ReflectionTestUtils.setField(config, "secret", "clave-de-prueba-suficientemente-larga-123456");
        ReflectionTestUtils.setField(config, "expirationMs", 900_000L);
        ReflectionTestUtils.setField(config, "refreshExpirationMs", 604_800_000L);
        provider = new JwtTokenProvider(config);
    }

    @Test
    void generaAccessConTipoAccessYDni() {
        String token = provider.generateToken("12345678", "ADMIN");

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getDniFromToken(token)).isEqualTo("12345678");
        assertThat(provider.getRolFromToken(token)).isEqualTo("ADMIN");
        assertThat(provider.getTipoFromToken(token)).isEqualTo(JwtTokenProvider.TIPO_ACCESS);
        assertThat(provider.esRefreshToken(token)).isFalse();
    }

    @Test
    void generaRefreshConTipoRefresh() {
        String refresh = provider.generateRefreshToken("12345678", "EMPLEADO");

        assertThat(provider.validateToken(refresh)).isTrue();
        assertThat(provider.getTipoFromToken(refresh)).isEqualTo(JwtTokenProvider.TIPO_REFRESH);
        assertThat(provider.esRefreshToken(refresh)).isTrue();
    }

    @Test
    void validaDniEsperado() {
        String token = provider.generateToken("12345678", "ADMIN");

        assertThat(provider.validateToken(token, "12345678")).isTrue();
        assertThat(provider.validateToken(token, "99999999")).isFalse();
    }

    @Test
    void tokenMalformadoNoEsValido() {
        assertThat(provider.validateToken("esto-no-es-un-jwt")).isFalse();
    }
}
