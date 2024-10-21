package raisetech.RecipeNotebook.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * レシピの材料のオブジェクトです。
 */
@Schema(description = "レシピの材料")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ingredient {

  private int id;

  private int recipeId;

  @NotBlank
  private String name;

  private String quantity;

  private boolean arrange;

}
