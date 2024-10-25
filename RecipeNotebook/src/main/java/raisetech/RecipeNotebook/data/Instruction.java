package raisetech.RecipeNotebook.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * レシピの作成手順のオブジェクトです。
 */
@Schema(description = "レシピの調理手順")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Instruction {

  private int id;

  public Instruction(int recipeId, int stepNumber, String content, boolean arrange) {
    this.recipeId = recipeId;
    this.stepNumber = stepNumber;
    this.content = content;
    this.arrange = arrange;
  }

  private int recipeId;

  private int stepNumber;

  @NotBlank
  private String content;

  private boolean arrange;

}
