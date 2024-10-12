package raisetech.RecipeNotebook.converter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeDetail;

/**
 * レシピ一覧・材料一覧・調理手順一覧をレシピ詳細情報一覧に変換するコンバーターです。
 */
@Component
public class RecipeConverter {

  public List<RecipeDetail> convertRecipeDetails(List<Recipe> recipes, List<Ingredient> ingredients,
      List<Instruction> instructions) {
    List<RecipeDetail> recipeDetails = new ArrayList<>();

    for (Recipe recipe : recipes) {
      RecipeDetail recipeDetail = new RecipeDetail();
      recipeDetail.setRecipe(recipe);

      List<Ingredient> convertIngredients = new ArrayList<>();
      for (Ingredient ingredient : ingredients) {
        if (ingredient.getRecipeId() == recipe.getId()) {
          convertIngredients.add(ingredient);
        }
      }
      recipeDetail.setIngredients(convertIngredients);

      List<Instruction> convertInstructions = new ArrayList<>();
      for (Instruction instruction : instructions) {
        if (instruction.getRecipeId() == recipe.getId()) {
          convertInstructions.add(instruction);
        }
      }
      recipeDetail.setInstructions(convertInstructions);

      recipeDetails.add(recipeDetail);
    }

    return recipeDetails;
  }

}
