package raisetech.RecipeNotebook.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeDetail;

class RecipeConverterTest {

  private RecipeConverter sut;

  @BeforeEach
  void before() {
    sut = new RecipeConverter();
  }

  @Test
  void 複数のレシピ_材料_調理手順からレシピIDが一致する情報を抽出してレシピ詳細情報の一覧に変換できること() {
    List<Recipe> recipes = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Recipe recipe = new Recipe();
      recipe.setId(i);
      recipes.add(recipe);
    }

    List<Ingredient> ingredients = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Ingredient ingredient = new Ingredient();
      ingredient.setRecipeId(i);
      ingredients.add(ingredient);
      ingredients.add(ingredient); // レシピIDが同じ材料を2つずつセットして検証
    }

    List<Instruction> instructions = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Instruction instruction = new Instruction();
      instruction.setRecipeId(i);
      instructions.add(instruction);
      instructions.add(instruction); // レシピIDが同じ調理手順を2つずつセットして検証
    }

    List<RecipeDetail> actual = sut.convertRecipeDetails(recipes, ingredients, instructions);

    assertAll(
        "Multiple assertions",
        () -> assertThat(actual, hasSize(2)),
        () -> assertThat(actual.get(0).getRecipe().getId(), is(1)),
        () -> assertThat(actual.get(1).getRecipe().getId(), is(2)),
        () -> {
          for (RecipeDetail actualValue : actual) {
            assertThat(actualValue.getIngredients(), hasSize(2));
            assertThat(actualValue.getInstructions(), hasSize(2));

            for (Ingredient ingredient : actualValue.getIngredients()) {
              assertThat(ingredient.getRecipeId(), is(actualValue.getRecipe().getId()));
            }
            assertThat(actualValue.getInstructions(), hasSize(2));

            for (Instruction instruction : actualValue.getInstructions()) {
              assertThat(instruction.getRecipeId(), is(actualValue.getRecipe().getId()));
            }
          }
        }
    );
  }

}
