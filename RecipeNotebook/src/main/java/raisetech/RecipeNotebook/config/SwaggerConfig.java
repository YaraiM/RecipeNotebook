package raisetech.RecipeNotebook.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("csrfToken"))
        .components(new Components()
            .addSecuritySchemes("csrfToken", new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("X-CSRF-TOKEN")
                .in(SecurityScheme.In.HEADER)
            )
        );
  }

}
