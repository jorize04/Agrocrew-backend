package pe.edu.upc.agrocrew.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI en /swagger-ui.html. Para probar endpoints protegidos:
 * 1) POST /api/v1/auth/login, 2) copiar el token, 3) botón "Authorize".
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "AgroCrew API", version = "v1",
                description = "API de evaluación de aptitud agrícola, riesgo hídrico y recomendación de cultivos"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
