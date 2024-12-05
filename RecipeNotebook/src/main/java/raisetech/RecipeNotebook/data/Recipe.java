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

  public Recipe(int userId, String name, String imagePath, String recipeSource, String servings,
      String remark, boolean favorite, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.userId = userId;
    this.name = name;
    this.imagePath = imagePath;
    this.recipeSource = recipeSource;
    this.servings = servings;
    this.remark = remark;
    this.favorite = favorite;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  private int id;

  private int userId;

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
