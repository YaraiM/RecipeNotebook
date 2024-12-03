package raisetech.RecipeNotebook.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.data.User;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
import raisetech.RecipeNotebook.exception.ResourceNotFoundException;
import raisetech.RecipeNotebook.repository.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

  @Mock
  private RecipeRepository repository;

  @Mock
  private CustomUserDetailsService customUserDetailsService;

  @Mock
  private FileStorageService fileStorageService;

  @InjectMocks
  private RecipeService sut;

  @ParameterizedTest
  @MethodSource("provideSearchRecipeTestCase")
  void レシピ詳細情報の一覧検索_検索条件に応じたレシピIDが返されかつメソッドが適切に呼び出されること(
      List<Integer> recipeIds, List<Integer> recipeIdsWithMatchingIngredients,
      List<Integer> expectedResultIds) {

    User user = createMockUser();

    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    List<Recipe> recipes = createMockRecipes(recipeIds);
    List<Ingredient> ingredients = createMockIngredients(recipeIds);
    List<Instruction> instructions = createMockInstructions(recipeIds);

    when(customUserDetailsService.getLoggedInUser()).thenReturn(user);
    when(repository.getRecipes(user.getId(), criteria)).thenReturn(recipes);

    if (!recipeIds.isEmpty()) {
      when(repository.getRecipeIdsWithMatchingIngredients(recipeIds,
          criteria.getIngredientNames())).thenReturn(recipeIdsWithMatchingIngredients);

      for (int i = 0; i < recipeIdsWithMatchingIngredients.size(); i++) {
        when(repository.getRecipe(anyInt())).thenReturn(recipes.get(i));
        when(repository.getIngredients(anyInt())).thenReturn(ingredients);
        when(repository.getInstructions(anyInt())).thenReturn(instructions);
      }
    }

    List<RecipeDetail> actual = sut.searchRecipeList(criteria);

    assertThat(actual, hasSize(expectedResultIds.size()));

    verify(repository, times(1)).getRecipes(user.getId(), criteria);
    if (!recipeIds.isEmpty()) {
      verify(repository, times(1)).getRecipeIdsWithMatchingIngredients(recipeIds,
          criteria.getIngredientNames());
    }

  }

  /**
   * レシピ詳細一覧検索のパラメータテストに適用するテストケースです。
   *
   * @return Argument
   */
  private static Stream<Arguments> provideSearchRecipeTestCase() {
    return Stream.of(
        Arguments.of(List.of(1, 2), List.of(1, 2), List.of(1, 2)),
        Arguments.of(List.of(1, 2), List.of(1), List.of(1)),
        Arguments.of(List.of(1, 2), List.of(), List.of()));
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
    User user = createMockUser();
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));
    RecipeDetail recipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn(
        "testPath");
    when(customUserDetailsService.getLoggedInUser()).thenReturn(user);

    MultipartFile mockFile = mock(MultipartFile.class);
    LocalDateTime testStartedTime = LocalDateTime.now();

    RecipeDetail actual = sut.createRecipeDetail(recipeDetail, mockFile);
    String actualImagePath = actual.getRecipe().getImagePath();
    LocalDateTime actualCreatedAt = actual.getRecipe().getCreatedAt();

    verify(fileStorageService, times(1)).storeFile(any(MultipartFile.class));
    verify(customUserDetailsService, times(1)).getLoggedInUser();
    verify(repository, times(1)).registerRecipe(recipe);
    verify(repository, times(2)).registerIngredient(any(Ingredient.class));
    verify(repository, times(2)).registerInstruction(any(Instruction.class));

    assertAll("Multiple assertions",
        () -> assertThat(actualImagePath, is("testPath")),
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
        });

  }

  @Test
  void レシピ詳細情報の更新_正常系_既存データの更新_リポジトリメソッドの呼び出しとイメージファイルパスの更新と更新日時の登録が適切に行われていること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

    RecipeDetail inputRecipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("testPath");
    when(repository.getIngredients(recipe.getId())).thenReturn(ingredients);
    when(repository.getInstructions(recipe.getId())).thenReturn(instructions);
    for (Ingredient ingredient : ingredients) {
      when(repository.getIngredient(ingredient.getId())).thenReturn(ingredient);
    }
    for (Instruction instruction : instructions) {
      when(repository.getInstruction(instruction.getId())).thenReturn(instruction);
    }

    MultipartFile mockFile = mock(MultipartFile.class);
    LocalDateTime testStartedTime = LocalDateTime.now();

    RecipeDetail actual = sut.updateRecipeDetail(inputRecipeDetail, mockFile);
    String actualImagePath = actual.getRecipe().getImagePath();
    LocalDateTime actualUpdatedAt = actual.getRecipe().getUpdatedAt();

    verify(repository, times(2)).getRecipe(recipe.getId());
    verify(repository, times(1)).getIngredients(recipe.getId());
    verify(repository, times(1)).getInstructions(recipe.getId());
    verify(repository, times(2)).getIngredient(anyInt());
    verify(repository, times(2)).getInstruction(anyInt());
    verify(repository, times(1)).updateRecipe(recipe);
    verify(repository, times(2)).updateIngredient(any(Ingredient.class));
    verify(repository, times(2)).updateInstruction(any(Instruction.class));
    verify(fileStorageService, times(1)).storeFile(any(MultipartFile.class));

    assertThat(actualImagePath, is("testPath"));
    assertThat(actualUpdatedAt.isAfter(testStartedTime) || actualUpdatedAt.isEqual(testStartedTime),
        is(true));

  }

  @Test
  void レシピ詳細情報の更新_正常系_材料調理手順の一部削除_リポジトリメソッドの呼び出しが適切に行われていること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> existingIngredients = createMockIngredients(List.of(1));
    List<Instruction> existingInstructions = createMockInstructions(List.of(1));

    Ingredient ingredient1 = existingIngredients.get(0);
    Ingredient ingredient2 = existingIngredients.get(1);
    Instruction instruction1 = existingInstructions.get(0);
    Instruction instruction2 = existingInstructions.get(1);

    List<Ingredient> inputIngredients = List.of(existingIngredients.get(0));
    List<Instruction> inputInstructions = List.of(existingInstructions.get(0));

    RecipeDetail inputRecipeDetail = new RecipeDetail(recipe, inputIngredients, inputInstructions);
    MultipartFile mockFile = mock(MultipartFile.class);

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("testPath");
    when(repository.getIngredients(recipe.getId())).thenReturn(existingIngredients);
    when(repository.getInstructions(recipe.getId())).thenReturn(existingInstructions);
    when(repository.getIngredient(ingredient1.getId())).thenReturn(ingredient1);
    when(repository.getInstruction(instruction1.getId())).thenReturn(instruction1);

    sut.updateRecipeDetail(inputRecipeDetail, mockFile);

    verify(repository, times(1)).deleteIngredient(ingredient2.getId());
    verify(repository, times(1)).deleteInstruction(instruction2.getId());

  }

  @Test
  void レシピ詳細情報の更新_正常系_材料調理手順の新規追加_リポジトリメソッドの呼び出しが適切に行われていること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();

    Ingredient newIngredient = new Ingredient(0, 1, "新しい材料", "大さじ１", false);
    Instruction newInstruction = new Instruction(0, 1, 3, "新しい手順", false);

    List<Ingredient> inputIngredients = List.of(newIngredient);
    List<Instruction> inputInstructions = List.of(newInstruction);

    RecipeDetail inputRecipeDetail = new RecipeDetail(recipe, inputIngredients, inputInstructions);
    MultipartFile mockFile = mock(MultipartFile.class);

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("testPath");
    when(repository.getIngredients(recipe.getId())).thenReturn(inputIngredients);
    when(repository.getInstructions(recipe.getId())).thenReturn(inputInstructions);

    sut.updateRecipeDetail(inputRecipeDetail, mockFile);

    verify(repository, times(1)).registerIngredient(newIngredient);
    verify(repository, times(1)).registerInstruction(newInstruction);
  }

  @Test
  void レシピ詳細情報の更新_異常系_存在しないレシピIDを指定して更新しようとした場合に例外がスローされること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

    RecipeDetail inputRecipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(repository.getRecipe(recipe.getId())).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.updateRecipeDetail(inputRecipeDetail, any(MultipartFile.class)));
    assertThat(e.getMessage(), is("レシピID「" + recipe.getId() + "」は存在しません"));

    verify(repository, times(1)).getRecipe(recipe.getId());

  }

  @Test
  void レシピ詳細情報の更新_異常系_存在しない材料IDを指定して更新しようとした場合に例外がスローされること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

    RecipeDetail inputRecipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(repository.getIngredients(recipe.getId())).thenReturn(ingredients);
    when(repository.getInstructions(recipe.getId())).thenReturn(instructions);
    when(repository.getIngredient(anyInt())).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.updateRecipeDetail(inputRecipeDetail, any(MultipartFile.class)));
    assertThat(e.getMessage(), is("材料ID「" + recipe.getId() + "」は存在しません"));

    verify(repository, times(1)).getRecipe(recipe.getId());
    verify(repository, times(1)).getIngredients(recipe.getId());
    verify(repository, times(1)).getInstructions(recipe.getId());
    verify(repository, times(1)).getIngredient(anyInt());

  }

  @Test
  void レシピ詳細情報の更新_異常系_存在しない調理手順IDを指定して更新しようとした場合に例外がスローされること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    List<Ingredient> ingredients = createMockIngredients(List.of(1));
    List<Instruction> instructions = createMockInstructions(List.of(1));

    RecipeDetail inputRecipeDetail = new RecipeDetail(recipe, ingredients, instructions);

    when(repository.getRecipe(recipe.getId())).thenReturn(recipe);
    when(repository.getIngredients(recipe.getId())).thenReturn(ingredients);
    when(repository.getInstructions(recipe.getId())).thenReturn(instructions);
    for (Ingredient ingredient : ingredients) {
      when(repository.getIngredient(ingredient.getId())).thenReturn(ingredient);
    }
    when(repository.getInstruction(anyInt())).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.updateRecipeDetail(inputRecipeDetail, any(MultipartFile.class)));
    assertThat(e.getMessage(), is("調理手順ID「" + recipe.getId() + "」は存在しません"));

    verify(repository, times(1)).getRecipe(recipe.getId());
    verify(repository, times(1)).getIngredients(recipe.getId());
    verify(repository, times(1)).getInstructions(recipe.getId());
    verify(repository, times(2)).getIngredient(anyInt());
    verify(repository, times(1)).getInstruction(anyInt());

  }

  @Test
  void お気に入りフラグの切替_正常系_IDに紐づくレシピのお気に入り切替メソッドが実行されること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    int id = recipe.getId();
    boolean favorite = recipe.isFavorite();

    when(repository.getRecipe(id)).thenReturn(recipe);

    sut.updateFavoriteStatus(id, favorite);

    verify(repository, times(1)).updateFavoriteStatus(id, favorite);

  }

  @Test
  void レシピの削除_正常系_IDに紐づくレシピ削除メソッドと画像ファイル削除が実行されること() {
    Recipe recipe = createMockRecipes(List.of(1)).getFirst();
    recipe.setImagePath("/uploads/test");
    int id = recipe.getId();

    when(repository.getRecipe(id)).thenReturn(recipe);

    sut.deleteRecipe(id);

    verify(repository, times(1)).getRecipe(id);
    verify(repository, times(1)).deleteRecipe(id);
    verify(fileStorageService, times(1)).deleteFile(recipe.getImagePath());

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
   * テスト用のユーザーを作成するメソッドです。
   *
   */
  private User createMockUser() {
    User user = new User();
    user.setId(1);

    return user;
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
      recipe.setImagePath("testPath" + id);
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
        ingredient.setId(i + 1);
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
        instruction.setId(i + 1);
        instruction.setRecipeId(id);
        instructions.add(instruction);
      }
    }
    return instructions;
  }

}
