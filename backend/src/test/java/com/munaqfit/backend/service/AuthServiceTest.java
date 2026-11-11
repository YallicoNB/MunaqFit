package com.munaqfit.backend.service;

import com.munaqfit.backend.config.JwtConfig;
import com.munaqfit.backend.dto.LoginRequest;
import com.munaqfit.backend.dto.LoginResponse;
import com.munaqfit.backend.exception.CustomExceptions;
import com.munaqfit.backend.model.Usuario;
import com.munaqfit.backend.repository.UsuarioRepository;
import com.munaqfit.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private JwtConfig jwtConfig;

    private AuthService authService;
    private Usuario usuario;
    private LoginRequest request;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtTokenProvider, jwtConfig);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setDni("12345678");
        usuario.setNombreCompleto("Noe Munaq");
        usuario.setEmail("noe@munaqfit.pe");
        usuario.setPassword("hash");
        usuario.setRol(Usuario.Rol.ADMIN);
        usuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        request = new LoginRequest();
        request.setDni("12345678");
        request.setPassword("12345678");
    }

    @Test
    void loginCorrectoDevuelveAccessYRefresh() {
        when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("12345678", "hash")).thenReturn(true);
        when(jwtTokenProvider.generateToken("12345678", "ADMIN")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("12345678", "ADMIN")).thenReturn("refresh-token");
        when(jwtConfig.getExpirationMs()).thenReturn(900_000L);

        LoginResponse respuesta = authService.login(request);

        assertThat(respuesta.getToken()).isEqualTo("access-token");
        assertThat(respuesta.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(respuesta.getRol()).isEqualTo("ADMIN");
        assertThat(respuesta.getExpiraEn()).isEqualTo(900_000L);
        assertThat(usuario.getIntentosFallidos()).isZero();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void loginConPasswordIncorrectaSumaIntentoYFalla() {
        when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("12345678", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomExceptions.UnauthorizedException.class);

        assertThat(usuario.getIntentosFallidos()).isEqualTo(1);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void tercerIntentoFallidoBloqueaLaCuenta() {
        usuario.setIntentosFallidos(2);
        when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("12345678", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomExceptions.UnauthorizedException.class)
                .hasMessageContaining("Demasiados intentos");

        assertThat(usuario.getBloqueadoHasta()).isAfter(LocalDateTime.now());
        assertThat(usuario.getIntentosFallidos()).isZero();
    }

    @Test
    void cuentaBloqueadaNoPermiteLogin() {
        usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(3));
        when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomExceptions.UnauthorizedException.class)
                .hasMessageContaining("Demasiados intentos");
    }

    @Test
    void usuarioInactivoNoPuedeEntrar() {
        usuario.setEstado(Usuario.EstadoUsuario.INACTIVO);
        when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomExceptions.UnauthorizedException.class);
    }

    @Test
    void refreshValidoRotaLosTokens() {
        when(jwtTokenProvider.validateToken("refresh-viejo")).thenReturn(true);
        when(jwtTokenProvider.esRefreshToken("refresh-viejo")).thenReturn(true);
        when(jwtTokenProvider.getDniFromToken("refresh-viejo")).thenReturn("12345678");
        when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generateToken("12345678", "ADMIN")).thenReturn("access-nuevo");
        when(jwtTokenProvider.generateRefreshToken("12345678", "ADMIN")).thenReturn("refresh-nuevo");
        when(jwtConfig.getExpirationMs()).thenReturn(900_000L);

        LoginResponse respuesta = authService.refrescarToken("refresh-viejo");

        assertThat(respuesta.getToken()).isEqualTo("access-nuevo");
        assertThat(respuesta.getRefreshToken()).isEqualTo("refresh-nuevo");
    }

    @Test
    void refreshInvalidoFalla() {
        when(jwtTokenProvider.validateToken("basura")).thenReturn(false);

        assertThatThrownBy(() -> authService.refrescarToken("basura"))
                .isInstanceOf(CustomExceptions.UnauthorizedException.class);
    }

    @Test
    void accessTokenNoSirveComoRefresh() {
        when(jwtTokenProvider.validateToken("access")).thenReturn(true);
        when(jwtTokenProvider.esRefreshToken("access")).thenReturn(false);

        assertThatThrownBy(() -> authService.refrescarToken("access"))
                .isInstanceOf(CustomExceptions.UnauthorizedException.class)
                .hasMessageContaining("no es un refresh");
    }
}
