package raisetech.RecipeNotebook.data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * レシピのオブジェクトです。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {

  private int id;

  @NotBlank
  private String name;

  private String image_path;

  private String recipeSource;

  private String servings;

  private String remark;

  private Boolean favorite;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

}
