package raisetech.RecipeNotebook.data;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * レシピの作成手順のオブジェクトです。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Instruction {

  private int id;

  private int recipeId;

  private int stepNumber;

  @NotBlank
  private String instruction;

  private Boolean arrange;

}
