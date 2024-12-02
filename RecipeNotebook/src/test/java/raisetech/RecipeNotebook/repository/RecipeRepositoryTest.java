package raisetech.RecipeNotebook.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;

@MybatisTest
@Transactional
class RecipeRepositoryTest {

  @Autowired
  private RecipeRepository sut;

  @ParameterizedTest
  @MethodSource("provideGetRecipeTestCase")
  void 検索条件に応じたレシピを検索できること(RecipeSearchCriteria criteria,
      List<Recipe> expectedRecipes) {

    int userId = 1;
    List<Recipe> actual = sut.getRecipes(userId, criteria);

    assertThat(actual, hasSize(expectedRecipes.size()));

    for (int i = 0; i < actual.size(); i++) {
      Recipe actualRecipe = actual.get(i);
      Recipe expectedRecipe = expectedRecipes.get(i);

      assertThat(actualRecipe.getId(), is(expectedRecipe.getId()));
      assertThat(actualRecipe.getUserId(), is(expectedRecipe.getUserId()));
      assertThat(actualRecipe.getName(), is(expectedRecipe.getName()));
      assertThat(actualRecipe.getImagePath(), is(expectedRecipe.getImagePath()));
      assertThat(actualRecipe.getRecipeSource(), is(expectedRecipe.getRecipeSource()));
      assertThat(actualRecipe.getServings(), is(expectedRecipe.getServings()));
      assertThat(actualRecipe.getRemark(), is(expectedRecipe.getRemark()));
      assertThat(actualRecipe.isFavorite(), is(expectedRecipe.isFavorite()));
    }
  }

  /**
   * レシピ取得のパラメータテストに適用するテストケースです。
   *
   * @return Argument
   */
  private static Stream<Arguments> provideGetRecipeTestCase() {
    return Stream.of(
        Arguments.of(
            new RecipeSearchCriteria(null, null, null,
                null, null, null, null),
            List.of(
                new Recipe(1, 1, "卵焼き", "/test-uploads/tamagoyaki_image.png",
                    "https://------1.com",
                    "2人分",
                    "備考欄1",
                    false,
                    LocalDateTime.parse("2024-09-22T17:00:00"),
                    LocalDateTime.parse("2024-09-22T17:00:00")),
                new Recipe(2, 1, "目玉焼き", "/test-uploads/medamayaki_image.png",
                    "https://------2.com", "1人分",
                    "備考欄2",
                    true,
                    LocalDateTime.parse("2024-09-23T17:00:00"),
                    LocalDateTime.parse("2024-10-23T17:00:00")))),
        Arguments.of(new RecipeSearchCriteria(List.of("卵焼き"), false,
                LocalDate.parse("2024-09-21"), LocalDate.parse("2024-09-23"),
                LocalDate.parse("2024-10-21"), LocalDate.parse("2024-10-23"), List.of("卵")),
            List.of(
                new Recipe(1, 1, "卵焼き", "/test-uploads/tamagoyaki_image.png",
                    "https://------1.com",
                    "2人分",
                    "備考欄1",
                    false,
                    LocalDateTime.parse("2024-09-22T17:00:00"),
                    LocalDateTime.parse("2024-09-22T17:00:00")))),
        Arguments.of(new RecipeSearchCriteria(List.of("卵", "焼"), false,
                LocalDate.parse("2024-09-21"), LocalDate.parse("2024-09-23"),
                LocalDate.parse("2024-10-21"), LocalDate.parse("2024-10-23"), List.of("卵", "砂糖")),
            List.of(
                new Recipe(1, 1, "卵焼き", "/test-uploads/tamagoyaki_image.png",
                    "https://------1.com",
                    "2人分",
                    "備考欄1",
                    false,
                    LocalDateTime.parse("2024-09-22T17:00:00"),
                    LocalDateTime.parse("2024-09-22T17:00:00")))),
        Arguments.of(new RecipeSearchCriteria(List.of("存在しないレシピ"), null,
                null, null, null, null, List.of("存在しない材料")),
            List.of())

    );
  }

  @Test
  void IDに紐づくレシピを取得できること() {
    Recipe actual = sut.getRecipe(1);

    assertRecipe(actual, 1, "卵焼き", "/test-uploads/tamagoyaki_image.png", "https://------1.com",
        "2人分",
        "備考欄1", false,
        LocalDateTime.parse("2024-09-22T17:00:00"), LocalDateTime.parse("2024-10-22T17:00:00"));
  }

  @ParameterizedTest
  @MethodSource("provideGetRecipeIdsWithMatchingIngredientsTestCase")
  void レシピIDに紐づきかつ指定したキーワードに合致する材料のID一覧が取得できること(
      List<Integer> recipeIds, List<String> ingredientNames,
      List<Integer> expectedIds) {
    List<Integer> actual = sut.getRecipeIdsWithMatchingIngredients(recipeIds, ingredientNames);

    assertThat(actual, hasSize(expectedIds.size()));

    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i), is(expectedIds.get(i)));
    }

  }

  /**
   * 材料取得のパラメータテストに適用するテストケースです。
   *
   * @return Argument
   */
  private static Stream<Arguments> provideGetRecipeIdsWithMatchingIngredientsTestCase() {
    return Stream.of(
        Arguments.of(List.of(1, 2), List.of(), List.of(1, 2),
            Arguments.of(List.of(1, 2), List.of("砂糖")), List.of(1),
            Arguments.of(List.of(1, 2), List.of("卵", "水")), List.of(2),
            Arguments.of(List.of(1, 2), List.of("存在しない材料"), List.of())
        ));
  }

  @Test
  void レシピIDに紐づく材料一覧を取得できること() {
    List<Ingredient> actual = sut.getIngredients(1);

    assertThat(actual.size(), is(4));
    assertIngredientDetail(actual.get(0), 1, "卵", "3個", false);
    assertIngredientDetail(actual.get(1), 1, "サラダ油", "適量", false);
    assertIngredientDetail(actual.get(2), 1, "醤油", "大さじ1/2", false);
    assertIngredientDetail(actual.get(3), 1, "砂糖", "大さじ1", false);

  }

  @Test
  void IDに紐づく材料を取得できること() {
    Ingredient actual = sut.getIngredient(1);

    assertIngredientDetail(actual, 1, "卵", "3個", false);

  }

  @Test
  void レシピIDに紐づく調理手順一覧を取得できること() {
    List<Instruction> actual = sut.getInstructions(1);

    assertThat(actual.size(), is(4));
    assertInstructionDetail(actual.get(0), 1, 1, "卵を溶いて調味料を混ぜ、卵液を作る", false);
    assertInstructionDetail(actual.get(1), 1, 2, "フライパンに油をたらし、火にかける", false);
    assertInstructionDetail(actual.get(2), 1, 3, "卵液を1/3くらいフライパンに入れて焼き、巻く",
        true);
    assertInstructionDetail(actual.get(3), 1, 4, "3の手順を繰り返して完成", false);

  }

  @Test
  void IDに紐づく調理手順を取得できること() {
    Instruction actual = sut.getInstruction(1);

    assertInstructionDetail(actual, 1, 1, "卵を溶いて調味料を混ぜ、卵液を作る", false);

  }

  @Test
  void レシピをデータベースに追加できること() {
    Recipe recipe = createSampleRecipe();
    int userId = recipe.getUserId();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    sut.registerRecipe(recipe);

    Recipe actual = sut.getRecipes(userId, criteria)
        .getLast(); // DB上のIDの値にかかわらず、最後に追加されたレコードを検証できるようにしている。
    assertRecipe(actual, 1, "ゆで卵", "test3/path", "https://------3.com", "1人分", "備考欄3", true,
        LocalDateTime.parse("2024-09-24T17:00:00"), null);

  }

  @Test
  void 材料をデータベースに追加できること() {
    Recipe recipe = createSampleRecipe();
    sut.registerRecipe(recipe);

    List<Ingredient> ingredients = createSampleIngredients();
    for (Ingredient ingredient : ingredients) {
      ingredient.setRecipeId(
          recipe.getId()); //　MySQLの仕様でオートインクリメントされたレシピID値はロールバック後も使用されないため、固定の数値（3）ではなくrecipeのIDを直接参照してセットする。
      sut.registerIngredient(ingredient);
    }

    List<Ingredient> actual = sut.getIngredients(
        recipe.getId());  //　MySQLの仕様でオートインクリメントされたレシピID値はロールバック後も使用されないため、固定の数値（3）ではなくrecipeのIDを直接参照する。
    assertIngredientDetail(actual.get(0), recipe.getId(), "卵", "1個",
        false);
    assertIngredientDetail(actual.get(1), recipe.getId(), "水", null, false);

  }

  @Test
  void 調理手順をデータベースに追加できること() {
    Recipe recipe = createSampleRecipe();
    sut.registerRecipe(recipe);

    List<Instruction> instructions = createSampleInstructions();
    for (Instruction instruction : instructions) {
      instruction.setRecipeId(
          recipe.getId()); //　MySQLの仕様でオートインクリメントされたレシピID値はロールバック後も使用されないため、固定の数値（3）ではなくrecipeのIDを直接参照してセットする。
      sut.registerInstruction(instruction);
    }

    List<Instruction> actual = sut.getInstructions(
        recipe.getId()); //　MySQLの仕様でオートインクリメントされたレシピID値はロールバック後も使用されないため、固定の数値（3）ではなくrecipeのIDを直接参照する。
    assertInstructionDetail(actual.get(0), recipe.getId(), 1,
        "鍋に卵がかぶるくらいの水を入れて沸騰させる", false);
    assertInstructionDetail(actual.get(1), recipe.getId(), 2,
        "卵を沸かした水に入れて7~12分茹でる。好みの硬さで時間を調整する", false);

  }

  @Test
  void 指定したIDのレシピを更新できること() {
    Recipe recipe = new Recipe();
    recipe.setId(1);
    recipe.setUserId(1);
    recipe.setName("卵焼きrev");
    recipe.setImagePath("test1/path/rev");
    recipe.setRecipeSource("https://------1/rev.com");
    recipe.setServings("2人分rev");
    recipe.setRemark("備考欄1rev");
    recipe.setFavorite(true);
    recipe.setUpdatedAt(LocalDateTime.parse("2024-11-24T17:00:00"));

    sut.updateRecipe(recipe);

    Recipe actual = sut.getRecipe(1);
    assertRecipe(actual, 1, "卵焼きrev", "test1/path/rev", "https://------1/rev.com", "2人分rev",
        "備考欄1rev", true, LocalDateTime.parse("2024-09-22T17:00:00"),
        LocalDateTime.parse("2024-11-24T17:00:00"));

  }

  @Test
  void 指定したIDの材料を更新できること() {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1);
    ingredient.setName("卵rev");
    ingredient.setQuantity("4個");
    ingredient.setArrange(true);

    sut.updateIngredient(ingredient);

    List<Ingredient> actual = sut.getIngredients(1);
    assertIngredientDetail(actual.get(0), 1, "卵rev", "4個", true);

  }

  @Test
  void 指定したIDの調理手順を更新できること() {
    Instruction instruction = new Instruction();
    instruction.setId(1);
    instruction.setStepNumber(2);
    instruction.setContent("卵を溶いて調味料を混ぜ、卵液を作るrev");
    instruction.setArrange(true);

    sut.updateInstruction(instruction);

    List<Instruction> actual = sut.getInstructions(1);
    assertInstructionDetail(actual.get(0), 1, 2, "卵を溶いて調味料を混ぜ、卵液を作るrev", true);

  }

  @Test
  void 指定したIDのレシピのお気に入り状態が切り替わること() {
    sut.updateFavoriteStatus(1, true);

    Recipe actual = sut.getRecipe(1);

    assertTrue(actual.isFavorite());

  }

  @Test
  void 指定したIDのレシピを削除できること() {
    int userId = 1;

    sut.deleteRecipe(1);
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();

    List<Recipe> actualAll = sut.getRecipes(1, criteria);
    Recipe actual = sut.getRecipe(1);

    assertAll("Multiple assertions", () -> assertThat(actualAll, hasSize(1)),
        () -> assertThat(actual, is(nullValue())));

  }

  @Test
  void 指定したIDの材料を削除できること() {
    sut.deleteIngredient(1);

    List<Ingredient> actual = sut.getAllIngredients();

    assertAll("Multiple assertions", () -> assertThat(actual, hasSize(6)),
        () -> assertThat(actual.stream().noneMatch(value -> value.getId() == 1), is(true)));

  }

  @Test
  void 指定したIDの調理手順を削除できること() {
    sut.deleteInstruction(1);

    List<Instruction> actual = sut.getAllInstructions();

    assertAll("Multiple assertions", () -> assertThat(actual, hasSize(6)),
        () -> assertThat(actual.stream().noneMatch(value -> value.getId() == 1), is(true)));

  }

  /**
   * レシピのアサーションを行うヘルパーメソッドです。
   */
  private void assertRecipe(Recipe recipe, int userId, String name, String imagePath,
      String recipeSource,
      String servings, String remark, Boolean favorite, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    assertAll("Multiple assertions",
        () -> assertThat(recipe.getUserId(), is(userId)),
        () -> assertThat(recipe.getName(), is(name)),
        () -> assertThat(recipe.getImagePath(), is(imagePath)),
        () -> assertThat(recipe.getRecipeSource(), is(recipeSource)),
        () -> assertThat(recipe.getServings(), is(servings)),
        () -> assertThat(recipe.getRemark(), is(remark)),
        () -> assertThat(recipe.isFavorite(), is(favorite)),
        () -> assertThat(recipe.getCreatedAt(), is(createdAt)),
        () -> assertThat(recipe.getUpdatedAt(), is(updatedAt)));
  }

  /**
   * 材料のアサーションを行うヘルパーメソッドです。
   */
  private void assertIngredientDetail(Ingredient ingredient, int recipeId, String name,
      String quantity, Boolean arrange) {
    assertAll("Multiple assertions", () -> assertThat(ingredient.getRecipeId(), is(recipeId)),
        () -> assertThat(ingredient.getName(), is(name)),
        () -> assertThat(ingredient.getQuantity(), is(quantity)),
        () -> assertThat(ingredient.isArrange(), is(arrange)));
  }

  /**
   * 調理手順のアサーションを行うヘルパーメソッドです。
   */
  private void assertInstructionDetail(Instruction instruction, int recipeId, int stepNumber,
      String content, Boolean arrange) {
    assertAll("Multiple assertions", () -> assertThat(instruction.getRecipeId(), is(recipeId)),
        () -> assertThat(instruction.getStepNumber(), is(stepNumber)),
        () -> assertThat(instruction.getContent(), is(content)),
        () -> assertThat(instruction.isArrange(), is(arrange)));
  }

  /**
   * テスト用のサンプルレシピです。
   */
  private static Recipe createSampleRecipe() {
    Recipe recipe = new Recipe();
    recipe.setUserId(1);
    recipe.setName("ゆで卵");
    recipe.setImagePath("test3/path");
    recipe.setRecipeSource("https://------3.com");
    recipe.setServings("1人分");
    recipe.setRemark("備考欄3");
    recipe.setFavorite(true);
    recipe.setCreatedAt(LocalDateTime.parse("2024-09-24T17:00:00"));
    recipe.setUpdatedAt(LocalDateTime.parse("2024-10-24T17:00:00"));
    return recipe;
  }

  /**
   * テスト用のサンプル材料リストです。
   * レシピオブジェクトのIDはDB上で自動付番されますが、MySQLの仕様でロールバック後も同じ値が二度と使用されないため、参照整合性エラーが起きないよう、この時点ではこの時点ではレシピIDを設定しません。
   */
  private static List<Ingredient> createSampleIngredients() {
    List<Ingredient> ingredients = new ArrayList<>();

    Ingredient ingredient1 = new Ingredient();
    ingredient1.setName("卵");
    ingredient1.setQuantity("1個");
    ingredient1.setArrange(false);

    Ingredient ingredient2 = new Ingredient();
    ingredient2.setName("水");
    ingredient2.setQuantity(null);
    ingredient2.setArrange(false);

    ingredients.add(ingredient1);
    ingredients.add(ingredient2);

    return ingredients;
  }

  /**
   * テスト用のサンプル調理手順リストです。
   * レシピオブジェクトのIDはDB上で自動付番されますが、MySQLの仕様でロールバック後も同じ値が二度と使用されないため、参照整合性エラーが起きないよう、この時点ではこの時点ではレシピIDを設定しません。
   */
  private static List<Instruction> createSampleInstructions() {
    List<Instruction> instructions = new ArrayList<>();

    Instruction instruction1 = new Instruction();
    instruction1.setStepNumber(1);
    instruction1.setContent("鍋に卵がかぶるくらいの水を入れて沸騰させる");
    instruction1.setArrange(false);

    Instruction instruction2 = new Instruction();
    instruction2.setStepNumber(2);
    instruction2.setContent("卵を沸かした水に入れて7~12分茹でる。好みの硬さで時間を調整する");
    instruction2.setArrange(false);

    instructions.add(instruction1);
    instructions.add(instruction2);

    return instructions;
  }

}
