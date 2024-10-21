package raisetech.RecipeNotebook.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * レシピのオブジェクトです。
 */
@Schema(description = "レシピ")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {

  private int id;

  @NotBlank
  private String name;

  private String imagePath;

  private String recipeSource;

  private String servings;

  private String remark;

  private boolean favorite;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

}
