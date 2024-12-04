package raisetech.RecipeNotebook.config;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
//@Profile("!ci")  // ciプロファイルではこのBeanを無効化
public class UploadDirectoryInitializer {

  @Value("${app.upload.dir}")
  private String uploadDir;

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(Paths.get(uploadDir));
      System.out.println("Upload directory: " + Paths.get(uploadDir).toAbsolutePath());
    } catch (IOException e) {
      throw new RuntimeException("Could not create upload directory!", e);
    }
  }
}
