package raisetech.RecipeNotebook.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import raisetech.RecipeNotebook.exception.ResourceNotFoundException;
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
    List<Recipe> allRecipes = createSampleRecipes();
    List<Ingredient> allIngredients = createSampleIngredients();
    List<Instruction> allInstructions = createSampleInstructions();

    when(repository.getAllRecipes()).thenReturn(allRecipes);
    when(repository.getAllIngredients()).thenReturn(allIngredients);
    when(repository.getAllInstructions()).thenReturn(allInstructions);

    List<RecipeDetail> actual = sut.searchRecipeList();

    verify(repository, times(1)).getAllRecipes();
    verify(repository, times(1)).getAllIngredients();
    verify(repository, times(1)).getAllInstructions();

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

  @Test
  void レシピ詳細情報の検索_正常系_IDに紐づくレシピ詳細情報を検索できること() {
    List<Recipe> allRecipes = createSampleRecipes();
    Recipe recipe = allRecipes.getFirst();

    List<Ingredient> allIngredients = createSampleIngredients();
    List<Ingredient> ingredients = allIngredients.stream()
        .filter(ingredient -> ingredient.getRecipeId() == recipe.getId())
        .toList();

    List<Instruction> allInstructions = createSampleInstructions();
    List<Instruction> instructions = allInstructions.stream()
        .filter(instruction -> instruction.getRecipeId() == recipe.getId())
        .toList();

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(repository.getIngredients(recipe.getId())).thenReturn(ingredients);
    when(repository.getInstructions(recipe.getId())).thenReturn(instructions);

    RecipeDetail actual = sut.searchRecipe(recipe.getId());

    verify(repository, times(1)).getRecipe(recipe.getId());
    verify(repository, times(1)).getIngredients(recipe.getId());
    verify(repository, times(1)).getInstructions(recipe.getId());

    assertAll(
        "Multiple assertions",
        () -> assertThat(actual.getRecipe().getId(), is(recipe.getId())),
        () -> {
          for (Ingredient ingredient : actual.getIngredients()) {
            assertThat(ingredient.getRecipeId(), is(actual.getRecipe().getId()));
          }
          for (Instruction instruction : actual.getInstructions()) {
            assertThat(instruction.getRecipeId(), is(actual.getRecipe().getId()));
          }
        }
    );

  }

  @Test
  void レシピ詳細情報の検索_異常系_存在しないレシピIDを指定した場合に例外がスローされること() {
    int id = 999;
    when(repository.getRecipe(id)).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.searchRecipe(id));
    assertThat(e.getMessage(), is("レシピID「" + id + "」は存在しません"));

    verify(repository, times(1)).getRecipe(id);
    verify(repository, never()).getIngredients(id);
    verify(repository, never()).getInstructions(id);

  }

  /**
   * テスト用のサンプルレシピ一覧です。
   *
   * @return レシピ一覧
   */
  private static List<Instruction> createSampleInstructions() {
    List<Instruction> instructions = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Instruction instruction = new Instruction();
      instruction.setRecipeId(i);
      instructions.add(instruction);
      instructions.add(instruction); // レシピIDが同じ調理手順を2つずつセットして検証
    }
    return instructions;
  }

  /**
   * テスト用のサンプル材料一覧です。
   *
   * @return 材料一覧
   */
  private static List<Ingredient> createSampleIngredients() {
    List<Ingredient> ingredients = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Ingredient ingredient = new Ingredient();
      ingredient.setRecipeId(i);
      ingredients.add(ingredient);
      ingredients.add(ingredient); // レシピIDが同じ材料を2つずつセットして検証
    }
    return ingredients;
  }

  /**
   * テスト用のサンプル調理手順一覧です。
   *
   * @return 調理手順一覧
   */
  private static List<Recipe> createSampleRecipes() {
    List<Recipe> recipes = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Recipe recipe = new Recipe();
      recipe.setId(i);
      recipes.add(recipe);
    }
    return recipes;
  }

}
