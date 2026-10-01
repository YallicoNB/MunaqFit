package com.munaqfit.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de la documentacion de la API (springdoc / OpenAPI).
 *
 * - Define el titulo y la version que se ven en la UI de Swagger.
 * - Registra el esquema de seguridad "bearerAuth" para que aparezca el boton
 *   "Authorize" y se pueda pegar el token JWT y probar los endpoints protegidos.
 *
 * No se declara "servers" a proposito: springdoc deduce el host de la peticion,
 * asi la UI funciona igual en local (localhost:8080) y en el servidor desplegado.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API MunaqFit",
                version = "1.0.0",
                description = "API REST de MunaqFit: autenticacion con JWT, catalogo de bebidas, "
                        + "punto de venta, ordenes, inventario, usuarios, proveedores, reportes y kardex.",
                contact = @Contact(name = "Equipo MunaqFit")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega aqui el token JWT. Se obtiene en POST /api/auth/login."
)
public class OpenApiConfig {
}
