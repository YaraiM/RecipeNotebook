package raisetech.RecipeNotebook;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
    info = @Info(title = "レシピノートAPI仕様", description = "レシピノートを管理するAPI仕様です",
        version = "0.1.0",
        license = @License(name = "Apache License 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.txt")),
    servers = {
        @Server(description = "Development Server", url = "http://localhost:8080"),
        @Server(description = "Production Server", url = "-")
    }
)
@SpringBootApplication
public class RecipeNotebookApplication {

  public static void main(String[] args) {
    SpringApplication.run(RecipeNotebookApplication.class, args);
  }

}
