package raisetech.RecipeNotebook.data;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * レシピの材料のオブジェクトです。
 */
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

  private String unit;

  private Boolean arrange;

}
