package raisetech.RecipeNotebook.domain;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;

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
