package raisetech.RecipeNotebook.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;

/**
 * レシピ詳細情報のオブジェクトです。レシピIDに紐づくレシピ、材料一覧、調理手順一覧をまとめます。
 */
@Schema(description = "レシピ詳細情報")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetail {

  @Valid
  private Recipe recipe;

  @Valid
  private List<Ingredient> ingredients;

  @Valid
  private List<Instruction> instructions;

}
