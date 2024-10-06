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
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.RecipeNotebook.converter.RecipeConverter;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
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

  @ParameterizedTest
  @MethodSource("provideSearchRecipeTestCase")
  void レシピ詳細情報の一覧検索_検索条件に応じてメソッドが適切に呼び出されること(
      List<Integer> ids, RecipeSearchCriteria criteria, int expectedResultCount
  ) {
    List<Recipe> recipes = createMockRecipes(ids);

    when(repository.getRecipes(criteria)).thenReturn(recipes);

    if (!ids.isEmpty()) {
      List<Ingredient> ingredients = createMockIngredients(ids);
      List<Instruction> instructions = createMockInstructions(ids);
      when(repository.getIngredientsByRecipeIds(ids, criteria)).thenReturn(ingredients);
      when(repository.getInstructionsByRecipeIds(ids, criteria)).thenReturn(instructions);

      for (Integer id : ids) {
        Recipe recipe = recipes.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
        List<Ingredient> recipeIngredients = ingredients.stream()
            .filter(ing -> ing.getRecipeId() == id).toList();
        List<Instruction> recipeInstructions = instructions.stream()
            .filter(ins -> ins.getRecipeId() == id).toList();

        when(repository.getRecipe(id)).thenReturn(recipe);
        when(repository.getIngredients(id)).thenReturn(recipeIngredients);
        when(repository.getInstructions(id)).thenReturn(recipeInstructions);
      }
    }

    List<RecipeDetail> actual = sut.searchRecipeList(criteria);

    assertThat(actual, hasSize(ids.size()));

    verify(repository, times(1)).getRecipes(criteria);
    if (!ids.isEmpty()) {
      verify(repository, times(1)).getIngredientsByRecipeIds(ids, criteria);
      verify(repository, times(1)).getInstructionsByRecipeIds(ids, criteria);

      for (int i = 0; i < expectedResultCount; i++) {
        int recipeId = ids.get(i);
        verify(repository, times(1)).getRecipe(recipeId);
        verify(repository, times(1)).getIngredients(recipeId);
        verify(repository, times(1)).getInstructions(recipeId);
      }
    } else {
      verify(repository, never()).getIngredientsByRecipeIds(any(), any());
      verify(repository, never()).getInstructionsByRecipeIds(any(), any());
    }

  }

  /**
   * レシピ詳細一覧検索のパラメータテストに適用するテストケースです。
   *
   * @return Argument
   */
  private static Stream<Arguments> provideSearchRecipeTestCase() {
    return Stream.of(Arguments.of(List.of(1, 2), new RecipeSearchCriteria("卵焼き", null), 2),
        Arguments.of(List.of(3), new RecipeSearchCriteria(null, "目玉焼き"), 1),
        Arguments.of(List.of(), new RecipeSearchCriteria("存在しないレシピ", null), 0));
  }

  @Test
  void レシピ詳細情報の検索_正常系_メソッドが適切に呼び出されレシピIDに紐づく情報が検索できること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(repository.getIngredients(recipe.getId())).thenReturn(ingredients);
    when(repository.getInstructions(recipe.getId())).thenReturn(instructions);

    RecipeDetail actual = sut.searchRecipeDetail(recipe.getId());

    verify(repository, times(1)).getRecipe(recipe.getId());
    verify(repository, times(1)).getIngredients(recipe.getId());
    verify(repository, times(1)).getInstructions(recipe.getId());

    assertAll("Multiple assertions",
        () -> assertThat(actual.getRecipe().getId(), is(recipe.getId())), () -> {
          for (Ingredient ingredient : actual.getIngredients()) {
            assertThat(ingredient.getRecipeId(), is(actual.getRecipe().getId()));
          }
          for (Instruction instruction : actual.getInstructions()) {
            assertThat(instruction.getRecipeId(), is(actual.getRecipe().getId()));
          }
        });

  }

  @Test
  void レシピ詳細情報の検索_異常系_存在しないレシピIDを指定した場合に例外がスローされること() {
    int id = 999;
    when(repository.getRecipe(id)).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.searchRecipeDetail(id));
    assertThat(e.getMessage(), is("レシピID「" + id + "」は存在しません"));

    verify(repository, times(1)).getRecipe(id);
    verify(repository, never()).getIngredients(id);
    verify(repository, never()).getInstructions(id);

  }

  @Test
  void レシピ詳細情報の新規登録_リポジトリメソッドの呼び出しと初期情報の登録が適切に行われていること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));
    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    LocalDateTime testStartedTime = LocalDateTime.now();

    RecipeDetail actual = sut.registerRecipeDetail(recipeDetail);
    LocalDateTime actualCreatedAt = actual.getRecipe().getCreatedAt();

    verify(repository, times(1)).registerRecipe(recipe);
    verify(repository, times(2)).registerIngredient(any(Ingredient.class));
    verify(repository, times(2)).registerInstruction(any(Instruction.class));

    assertAll("Multiple assertions", () -> assertThat(
        actualCreatedAt.isAfter(testStartedTime) || actualCreatedAt.isEqual(testStartedTime),
        is(true)), () -> {
      for (Ingredient ingredient : actual.getIngredients()) {
        assertThat(ingredient.getRecipeId(), is(actual.getRecipe().getId()));
      }
      for (int i = 0; i < actual.getInstructions().size(); i++) {
        Instruction instruction = actual.getInstructions().get(i);
        assertThat(instruction.getRecipeId(), is(actual.getRecipe().getId()));
        assertThat(instruction.getStepNumber(), is(i + 1));
      }
    });

  }

  @Test
  void レシピ詳細情報の更新_正常系_リポジトリメソッドの呼び出しと更新日時の登録が適切に行われていること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    Ingredient ingredient1 = ingredients.get(0);
    Ingredient ingredient2 = ingredients.get(1);

    Instruction instruction1 = instructions.get(0);
    Instruction instruction2 = instructions.get(1);

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

    assertThat(actualUpdatedAt.isAfter(testStartedTime) || actualUpdatedAt.isEqual(testStartedTime),
        is(true));

  }

  @Test
  void レシピ詳細情報の更新_異常系_存在しないレシピIDを指定した場合に例外がスローされること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

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
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    Ingredient ingredient = ingredients.get(0);

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
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    Ingredient ingredient1 = ingredients.get(0);
    Ingredient ingredient2 = ingredients.get(1);

    Instruction instruction = instructions.get(0);

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

  @Test
  void レシピの削除_正常系_IDに紐づくレシピ削除メソッドが実行されること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    int id = recipe.getId();

    when(repository.getRecipe(id)).thenReturn(recipe);

    sut.deleteRecipe(id);

    verify(repository, times(1)).getRecipe(id);
    verify(repository, times(1)).deleteRecipe(id);

  }

  @Test
  void レシピの削除_異常系_存在しないIDを指定した場合に例外がスローされること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    int id = recipe.getId();

    when(repository.getRecipe(id)).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.deleteRecipe(id));
    assertThat(e.getMessage(), is("レシピID「" + id + "」は存在しません"));

    verify(repository, times(1)).getRecipe(id);
    verify(repository, never()).deleteRecipe(id);

  }

  @Test
  void 材料の削除_正常系_IDに紐づく材料削除メソッドが実行されること() {
    Ingredient ingredient = new Ingredient();
    int id = 1;
    ingredient.setId(id);

    when(repository.getIngredient(id)).thenReturn(ingredient);

    sut.deleteIngredient(id);

    verify(repository, times(1)).getIngredient(id);
    verify(repository, times(1)).deleteIngredient(id);

  }

  @Test
  void 材料の削除_異常系_存在しないIDを指定した場合に例外がスローされること() {
    Ingredient ingredient = new Ingredient();
    int id = 999;
    ingredient.setId(id);

    when(repository.getIngredient(id)).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.deleteIngredient(id));
    assertThat(e.getMessage(), is("材料ID「" + id + "」は存在しません"));

    verify(repository, times(1)).getIngredient(id);
    verify(repository, never()).deleteIngredient(id);

  }

  @Test
  void 調理手順の削除_正常系_IDに紐づく調理手順削除メソッドが実行されること() {
    Instruction instruction = new Instruction();
    int id = 1;
    instruction.setId(id);

    when(repository.getInstruction(id)).thenReturn(instruction);

    sut.deleteInstruction(id);

    verify(repository, times(1)).getInstruction(id);
    verify(repository, times(1)).deleteInstruction(id);

  }

  @Test
  void 調理手順の削除_異常系_存在しないIDを指定した場合に例外がスローされること() {
    Instruction instruction = new Instruction();
    int id = 999;
    instruction.setId(id);

    when(repository.getInstruction(id)).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.deleteInstruction(id));
    assertThat(e.getMessage(), is("調理手順ID「" + id + "」は存在しません"));

    verify(repository, times(1)).getInstruction(id);
    verify(repository, never()).deleteInstruction(id);

  }

  /**
   * テスト用のサンプルレシピ一覧を作成するメソッドです。
   *
   * @return レシピ一覧
   */
  private List<Recipe> createMockRecipes(List<Integer> ids) {
    List<Recipe> recipes = new ArrayList<>();
    for (Integer id : ids) {
      Recipe recipe = new Recipe();
      recipe.setId(id);
      recipes.add(recipe);
    }
    return recipes;
  }

  /**
   * テスト用のサンプル材料一覧を作成するメソッドです。
   *
   * @return 材料
   */
  private List<Ingredient> createMockIngredients(List<Integer> ids) {
    List<Ingredient> ingredients = new ArrayList<>();
    for (Integer id : ids) {
      for (int i = 0; i < 2; i++) {
        Ingredient ingredient = new Ingredient();
        ingredient.setRecipeId(id);
        ingredients.add(ingredient);
      }
    }
    return ingredients;
  }

  /**
   * テスト用のサンプル調理手順一覧を作成するメソッドです。
   *
   * @return 調理手順一覧
   */
  private List<Instruction> createMockInstructions(List<Integer> ids) {
    List<Instruction> instructions = new ArrayList<>();
    for (Integer id : ids) {
      for (int i = 0; i < 2; i++) {
        Instruction instruction = new Instruction();
        instruction.setRecipeId(id);
        instructions.add(instruction);
      }
    }
    return instructions;
  }

}
