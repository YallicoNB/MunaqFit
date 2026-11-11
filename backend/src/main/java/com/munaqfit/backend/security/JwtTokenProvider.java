package com.munaqfit.backend.security;

import com.munaqfit.backend.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        byte[] keyBytes = jwtConfig.jwtSecretKey().getEncoded();
        if (keyBytes.length < 32) {
            keyBytes = Base64.getEncoder().encode(jwtConfig.jwtSecretKey().getEncoded());
        }
        this.secretKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    public String generateToken(String dni, String rol) {
        return generar(dni, rol, jwtConfig.getExpirationMs(), TIPO_ACCESS);
    }

    /**
     * Refresh token de vida larga (7 dias por defecto). Lleva el claim
     * "tipo=refresh" para que no pueda usarse como access token en las
     * rutas protegidas.
     */
    public String generateRefreshToken(String dni, String rol) {
        return generar(dni, rol, jwtConfig.getRefreshExpirationMs(), TIPO_REFRESH);
    }

    private String generar(String dni, String rol, long duracionMs, String tipo) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + duracionMs);

        return Jwts.builder()
                .subject(dni)
                .claim("rol", rol)
                .claim("tipo", tipo)
                .id(java.util.UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(this.secretKey)
                .compact();
    }

    public String getDniFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRolFromToken(String token) {
        Object rol = parseClaims(token).get("rol");
        return rol != null ? rol.toString() : null;
    }

    /**
     * "access" o "refresh". Los tokens anteriores a la etapa 3 no traian el
     * claim: se asumen access para no invalidar sesiones ya abiertas.
     */
    public String getTipoFromToken(String token) {
        Object tipo = parseClaims(token).get("tipo");
        return tipo != null ? tipo.toString() : TIPO_ACCESS;
    }

    public boolean esRefreshToken(String token) {
        return TIPO_REFRESH.equals(getTipoFromToken(token));
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token, String expectedDni) {
        String dni = getDniFromToken(token);
        return dni.equals(expectedDni) && validateToken(token);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
