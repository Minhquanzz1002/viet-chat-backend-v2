package vn.edu.iuh.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(name = "Nguyễn Minh Quân",email = "quannguyenminh1001@gmail.com"),
                description = "API cho website, mobile app Việt Chat",
                title = "API Việt Chat",
                version = "v1.0.0"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT authentication ",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
