package raisetech.RecipeNotebook.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger-UIのAPI仕様書における認証・認可の設定です。
 */
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
        .addSecurityItem(new SecurityRequirement().addList("csrfToken"))
        .components(new Components()
            .addSecuritySchemes("basicAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")

            )
            .addSecuritySchemes("csrfToken", new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("X-CSRF-TOKEN")
                .in(SecurityScheme.In.HEADER))
        );
  }

}
