package raisetech.RecipeNotebook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("!ci")
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload.dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // /uploads/** へのリクエストを実際のアップロードディレクトリにマッピング
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir);
  }
}
