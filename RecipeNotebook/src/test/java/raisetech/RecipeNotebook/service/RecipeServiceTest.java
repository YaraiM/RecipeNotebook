package raisetech.RecipeNotebook.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

  @Test
  void レシピ詳細情報の更新_正常系_リポジトリメソッドの呼び出しと更新日時の登録が適切に行われていること() {
    Recipe recipe = createSampleRecipe();

    List<Ingredient> ingredients = createSampleIngredients(recipe);
    Ingredient ingredient1 = ingredients.get(0);
    Ingredient ingredient2 = ingredients.get(1);

    List<Instruction> instructions = createSampleInstructions(recipe);
    Instruction instruction1 = instructions.get(0);
    Instruction instruction2 = instructions.get(1);

    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(repository.getIngredient(ingredient1.getId())).thenReturn(ingredient1);
    when(repository.getIngredient(ingredient2.getId())).thenReturn(ingredient2);
    when(repository.getInstruction(instruction1.getId())).thenReturn(instruction1);
    when(repository.getInstruction(instruction2.getId())).thenReturn(instruction2);

    LocalDateTime testStartedTime = LocalDateTime.now();

    RecipeDetail actual = sut.updateRecipeDetail(recipeDetail);
    LocalDateTime actualUpdatedAt = actual.getRecipe().getUpdatedAt();

    verify(repository, times(1)).getRecipe(recipe.getId());
    verify(repository, times(2)).getIngredient(anyInt());
    verify(repository, times(2)).getInstruction(anyInt());
    verify(repository, times(1)).updateRecipe(recipe);
    verify(repository, times(2)).updateIngredient(any(Ingredient.class));
    verify(repository, times(2)).updateInstruction(any(Instruction.class));

    assertThat(actualUpdatedAt.isAfter(testStartedTime)
        || actualUpdatedAt.isEqual(testStartedTime), is(true));

  }

  @Test
  void レシピ詳細情報の更新_異常系_存在しないレシピIDを指定した場合に例外がスローされること() {
    Recipe recipe = createSampleRecipe();
    List<Ingredient> ingredients = createSampleIngredients(recipe);
    List<Instruction> instructions = createSampleInstructions(recipe);
    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(repository.getRecipe(recipe.getId())).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.updateRecipeDetail(recipeDetail));
    assertThat(e.getMessage(), is("レシピID「" + recipe.getId() + "」は存在しません"));

    verify(repository, times(1)).getRecipe(recipe.getId());
    verify(repository, never()).getIngredient(anyInt());
    verify(repository, never()).getInstruction(anyInt());
    verify(repository, never()).updateRecipe(recipe);
    verify(repository, never()).updateIngredient(any(Ingredient.class));
    verify(repository, never()).updateInstruction(any(Instruction.class));

  }

  @Test
  void レシピ詳細情報の更新_異常系_存在しない材料IDを指定した場合に例外がスローされること() {
    Recipe recipe = createSampleRecipe();

    List<Ingredient> ingredients = createSampleIngredients(recipe);
    Ingredient ingredient = ingredients.get(0);

    List<Instruction> instructions = createSampleInstructions(recipe);

    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(repository.getIngredient(ingredient.getId())).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.updateRecipeDetail(recipeDetail));
    assertThat(e.getMessage(), is("材料ID「" + ingredient.getId() + "」は存在しません"));

    verify(repository, times(1)).getRecipe(recipe.getId());
    verify(repository, times(1)).getIngredient(ingredient.getId());
    verify(repository, never()).getInstruction(anyInt());
    verify(repository, never()).updateRecipe(recipe);
    verify(repository, never()).updateIngredient(any(Ingredient.class));
    verify(repository, never()).updateInstruction(any(Instruction.class));

  }

  @Test
  void レシピ詳細情報の更新_異常系_存在しない調理手順IDを指定した場合に例外がスローされること() {
    Recipe recipe = createSampleRecipe();

    List<Ingredient> ingredients = createSampleIngredients(recipe);
    Ingredient ingredient1 = ingredients.get(0);
    Ingredient ingredient2 = ingredients.get(1);

    List<Instruction> instructions = createSampleInstructions(recipe);
    Instruction instruction = instructions.get(0);

    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(repository.getIngredient(ingredient1.getId())).thenReturn(ingredient1);
    when(repository.getIngredient(ingredient2.getId())).thenReturn(ingredient2);
    when(repository.getInstruction(instruction.getId())).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.updateRecipeDetail(recipeDetail));
    assertThat(e.getMessage(), is("調理手順ID「" + instruction.getId() + "」は存在しません"));

    verify(repository, times(1)).getRecipe(recipe.getId());
    verify(repository, times(2)).getIngredient(anyInt());
    verify(repository, times(1)).getInstruction(instruction.getId());
    verify(repository, never()).updateRecipe(recipe);
    verify(repository, never()).updateIngredient(any(Ingredient.class));
    verify(repository, never()).updateInstruction(any(Instruction.class));

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
   * テスト用のサンプルレシピに紐づくサンプル材料一覧を作成するメソッドです。
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
   * テスト用のサンプルレシピに紐づくサンプル調理手順一覧を作成するメソッドです。
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
