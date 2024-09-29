package raisetech.RecipeNotebook.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
    List<Recipe> allRecipes = createAllSampleRecipes();
    List<Ingredient> allIngredients = createAllSampleIngredients();
    List<Instruction> allInstructions = createAllSampleInstructions();

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
    Recipe recipe = createSampleRecipe();
    List<Ingredient> ingredients = createSampleIngredients(recipe);
    List<Instruction> instructions = createSampleInstructions(recipe);

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

  @Test
  void レシピ詳細情報の新規登録_リポジトリメソッドの呼び出しと初期情報の登録が適切に行われていること() {
    Recipe recipe = createSampleRecipe();
    List<Ingredient> ingredients = createSampleIngredients(recipe);
    List<Instruction> instructions = createSampleInstructions(recipe);
    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    LocalDateTime testStartedTime = LocalDateTime.now();

    RecipeDetail actual = sut.registerRecipeDetail(recipeDetail);
    LocalDateTime actualCreatedAt = actual.getRecipe().getCreatedAt();

    verify(repository, times(1)).registerRecipe(recipe);
    verify(repository, times(2)).registerIngredient(any(Ingredient.class));
    verify(repository, times(2)).registerInstruction(any(Instruction.class));

    assertAll(
        "Multiple assertions",
        () -> assertThat(
            actualCreatedAt.isAfter(testStartedTime) || actualCreatedAt.isEqual(testStartedTime),
            is(true)),
        () -> {
          for (Ingredient ingredient : actual.getIngredients()) {
            assertThat(ingredient.getRecipeId(), is(actual.getRecipe().getId()));
          }
          for (int i = 0; i < actual.getInstructions().size(); i++) {
            Instruction instruction = actual.getInstructions().get(i);
            assertThat(instruction.getRecipeId(), is(actual.getRecipe().getId()));
            assertThat(instruction.getStepNumber(), is(i + 1));
          }
        }
    );

  }

  /**
   * テスト用のサンプルレシピ一覧を作成するメソッドです。
   *
   * @return レシピ一覧
   */
  private static List<Recipe> createAllSampleRecipes() {
    List<Recipe> recipes = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Recipe recipe = new Recipe();
      recipe.setId(i);
      recipes.add(recipe);
    }
    return recipes;
  }

  /**
   * テスト用のサンプルレシピを作成するメソッドです。
   *
   * @return レシピ
   */
  private static Recipe createSampleRecipe() {
    List<Recipe> allRecipes = createAllSampleRecipes();
    return allRecipes.getFirst();
  }

  /**
   * テスト用のサンプル材料一覧を作成するメソッドです。
   *
   * @return 材料一覧
   */
  private static List<Ingredient> createAllSampleIngredients() {
    List<Ingredient> ingredients = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Ingredient ingredient1 = new Ingredient();
      ingredient1.setRecipeId(i);
      ingredients.add(ingredient1);

      Ingredient ingredient2 = new Ingredient();
      ingredient2.setRecipeId(i);
      ingredients.add(ingredient2);

    }
    return ingredients;
  }

  /**
   * テスト用のサンプル材料を作成するメソッドです。
   *
   * @return 材料
   */
  private static List<Ingredient> createSampleIngredients(Recipe recipe) {
    List<Ingredient> allIngredients = createAllSampleIngredients();
    return allIngredients.stream()
        .filter(ingredient -> ingredient.getRecipeId() == recipe.getId())
        .toList();
  }

  /**
   * テスト用のサンプル調理手順一覧を作成するメソッドです。
   *
   * @return 調理手順一覧
   */
  private static List<Instruction> createAllSampleInstructions() {
    List<Instruction> instructions = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      Instruction instruction1 = new Instruction();
      instruction1.setRecipeId(i);
      instructions.add(instruction1);

      Instruction instruction2 = new Instruction();
      instruction2.setRecipeId(i);
      instructions.add(instruction2);
    }
    return instructions;
  }

  /**
   * テスト用のサンプル調理手順を作成するメソッドです。
   *
   * @return 調理手順
   */
  private static List<Instruction> createSampleInstructions(Recipe recipe) {
    List<Instruction> allInstructions = createAllSampleInstructions();
    return allInstructions.stream()
        .filter(instruction -> instruction.getRecipeId() == recipe.getId())
        .toList();
  }

}
