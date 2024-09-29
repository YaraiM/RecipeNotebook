package raisetech.RecipeNotebook.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.RecipeNotebook.converter.RecipeConverter;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.repository.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

  @Mock
  private RecipeRepository repository;

  private RecipeService sut; // Mockitoを使用時は@Autowiredが適切に動作しない場合がある

  @BeforeEach
  void setUp() {
    RecipeConverter recipeConverter = new RecipeConverter(); // converterはモック化せずにインスタンス化
    sut = new RecipeService(repository, recipeConverter); // sutを手動でインスタンス化（repositoryとconverterを注入）
  }

  @Test
  void レシピ詳細情報の一覧検索_全件検索できること() {
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

    when(repository.getAllRecipes()).thenReturn(recipes);
    when(repository.getAllIngredients()).thenReturn(ingredients);
    when(repository.getAllInstructions()).thenReturn(instructions);

    List<RecipeDetail> actual = sut.searchRecipeList();

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
