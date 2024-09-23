package raisetech.RecipeNotebook;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition
@SpringBootApplication
public class RecipeNotebookApplication {

  public static void main(String[] args) {
    SpringApplication.run(RecipeNotebookApplication.class, args);
  }

}
