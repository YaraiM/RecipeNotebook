package raisetech.RecipeNotebook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload.dir}")
  private String uploadDir;

  @Value("${app.feature.someClassEnabled:true}")
  private boolean activeProfile;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if (activeProfile) {
      // /uploads/** へのリクエストを実際のアップロードディレクトリにマッピング
      registry.addResourceHandler("/uploads/**")
          .addResourceLocations("file:" + uploadDir);
    }
  }
}
