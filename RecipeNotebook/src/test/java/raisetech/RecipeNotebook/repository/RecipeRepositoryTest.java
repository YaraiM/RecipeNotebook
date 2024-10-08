package raisetech.RecipeNotebook.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;

@MybatisTest
@Transactional
class RecipeRepositoryTest {

  @Autowired
  private RecipeRepository sut;

  @Test
  void レシピを全件取得できること() {
    List<Recipe> actual = sut.getAllRecipes();

    assertThat(actual.size(), is(2));
    assertRecipeDetail(actual.get(0), "卵焼き", "test1/path", "https://------1.com", "2人分",
        "備考欄1", false, LocalDateTime.parse("2024-09-22T17:00:00"),
        LocalDateTime.parse("2024-10-22T17:00:00"));
    assertRecipeDetail(actual.get(1), "目玉焼き", "test2/path", "https://------2.com", "1人分",
        "備考欄2", true, LocalDateTime.parse("2024-09-23T17:00:00"),
        LocalDateTime.parse("2024-10-23T17:00:00"));
  }

  @Test
  void IDに紐づくレシピを取得できること() {
    Recipe actual = sut.getRecipe(1);

    assertRecipeDetail(actual, "卵焼き", "test1/path", "https://------1.com", "2人分",
        "備考欄1", false, LocalDateTime.parse("2024-09-22T17:00:00"),
        LocalDateTime.parse("2024-10-22T17:00:00"));
  }

  @Test
  void 材料を全件取得できること() {
    List<Ingredient> actual = sut.getAllIngredients();

    assertThat(actual.size(), is(7));
    assertIngredientDetail(actual.get(0), 1, "卵", BigDecimal.valueOf(3.0), "個", false);
    assertIngredientDetail(actual.get(1), 1, "サラダ油", null, null, false);
    assertIngredientDetail(actual.get(2), 1, "醤油", BigDecimal.valueOf(0.5), "大さじ", false);
    assertIngredientDetail(actual.get(3), 1, "砂糖", BigDecimal.valueOf(1.0), "大さじ", false);
    assertIngredientDetail(actual.get(4), 2, "卵", BigDecimal.valueOf(1.0), "個", false);
    assertIngredientDetail(actual.get(5), 2, "サラダ油", null, null, false);
    assertIngredientDetail(actual.get(6), 2, "水", null, null, false);

  }

  @Test
  void レシピIDに紐づく材料を取得できること() {
    List<Ingredient> actual = sut.getIngredients(1);

    assertThat(actual.size(), is(4));
    assertIngredientDetail(actual.get(0), 1, "卵", BigDecimal.valueOf(3.0), "個", false);
    assertIngredientDetail(actual.get(1), 1, "サラダ油", null, null, false);
    assertIngredientDetail(actual.get(2), 1, "醤油", BigDecimal.valueOf(0.5), "大さじ", false);
    assertIngredientDetail(actual.get(3), 1, "砂糖", BigDecimal.valueOf(1.0), "大さじ", false);

  }

  @Test
  void 調理手順を全件取得できること() {
    List<Instruction> actual = sut.getAllInstructions();

    assertThat(actual.size(), is(7));
    assertInstructionDetail(actual.get(0), 1, 1, "卵を溶いて調味料を混ぜ、卵液を作る", false);
    assertInstructionDetail(actual.get(1), 1, 2, "フライパンに油をたらし、火にかける", false);
    assertInstructionDetail(actual.get(2), 1, 3, "卵液を1/3くらいフライパンに入れて焼き、巻く",
        true);
    assertInstructionDetail(actual.get(3), 1, 4, "3の手順を繰り返して完成", false);
    assertInstructionDetail(actual.get(4), 2, 1, "フライパンに油をたらし、火にかける", false);
    assertInstructionDetail(actual.get(5), 2, 2, "フライパンに卵を割り入れる", false);
    assertInstructionDetail(actual.get(6), 2, 3,
        "少し焼けたら水を入れ、ふたをして5分、弱火にかけて完成", false);

  }

  @Test
  void レシピIDに紐づく調理手順を取得できること() {
    List<Instruction> actual = sut.getInstructions(1);

    assertThat(actual.size(), is(4));
    assertInstructionDetail(actual.get(0), 1, 1, "卵を溶いて調味料を混ぜ、卵液を作る", false);
    assertInstructionDetail(actual.get(1), 1, 2, "フライパンに油をたらし、火にかける", false);
    assertInstructionDetail(actual.get(2), 1, 3, "卵液を1/3くらいフライパンに入れて焼き、巻く",
        true);
    assertInstructionDetail(actual.get(3), 1, 4, "3の手順を繰り返して完成", false);

  }

  @Test
  void レシピをデータベースに追加できること() {
    Recipe recipe = createSampleRecipe();
    sut.registerRecipe(recipe);

    Recipe actual = sut.getAllRecipes()
        .getLast(); // DB上のIDの値にかかわらず、最後に追加されたレコードを検証できるようにしている。
    assertRecipeDetail(actual, "ゆで卵", "test3/path", "https://------3.com", "1人分",
        "備考欄3", true, LocalDateTime.parse("2024-09-24T17:00:00"),
        null);

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
    assertIngredientDetail(actual.get(0), recipe.getId(), "卵", BigDecimal.valueOf(1.0), "個",
        false);
    assertIngredientDetail(actual.get(1), recipe.getId(), "水", null, null, false);

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
        "鍋に卵がかぶるくらいの水を入れて沸騰させる",
        false);
    assertInstructionDetail(actual.get(1), recipe.getId(), 2,
        "卵を沸かした水に入れて7~12分茹でる。好みの硬さで時間を調整する", false);

  }

  @Test
  void 指定したIDのレシピを更新できること() {
    Recipe recipe = new Recipe();
    recipe.setId(1);
    recipe.setName("卵焼きrev");
    recipe.setImagePath("test1/path/rev");
    recipe.setRecipeSource("https://------1/rev.com");
    recipe.setServings("2人分rev");
    recipe.setRemark("備考欄1rev");
    recipe.setFavorite(true);
    recipe.setUpdatedAt(LocalDateTime.parse("2024-11-24T17:00:00"));

    sut.updateRecipe(recipe);

    Recipe actual = sut.getRecipe(1);
    assertRecipeDetail(actual, "卵焼きrev", "test1/path/rev", "https://------1/rev.com", "2人分rev",
        "備考欄1rev", true, LocalDateTime.parse("2024-09-22T17:00:00"),
        LocalDateTime.parse("2024-11-24T17:00:00"));

  }

  @Test
  void 指定したIDの材料を更新できること() {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1);
    ingredient.setName("卵rev");
    ingredient.setQuantity(BigDecimal.valueOf(4));
    ingredient.setUnit("個rev");
    ingredient.setArrange(true);

    sut.updateIngredient(ingredient);

    List<Ingredient> actual = sut.getIngredients(1);
    assertIngredientDetail(actual.get(0), 1, "卵rev", BigDecimal.valueOf(4.0), "個rev",
        true);

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
  void 指定したIDのレシピを削除できること() {
    sut.deleteRecipe(1);

    List<Recipe> actualAll = sut.getAllRecipes();
    Recipe actual = sut.getRecipe(1);

    assertAll(
        "Multiple assertions",
        () -> assertThat(actualAll, hasSize(1)),
        () -> assertThat(actual, is(nullValue()))
    );

  }

  @Test
  void 指定したIDの材料を削除できること() {
    sut.deleteIngredient(1);

    List<Ingredient> actual = sut.getAllIngredients();

    assertAll(
        "Multiple assertions",
        () -> assertThat(actual, hasSize(6)),
        () -> assertThat(actual.stream().noneMatch(value -> value.getId() == 1), is(true))
    );

  }

  @Test
  void 指定したIDの調理手順を削除できること() {
    sut.deleteInstruction(1);

    List<Instruction> actual = sut.getAllInstructions();

    assertAll(
        "Multiple assertions",
        () -> assertThat(actual, hasSize(6)),
        () -> assertThat(actual.stream().noneMatch(value -> value.getId() == 1), is(true))
    );

  }

  /**
   * レシピのアサーションを行うヘルパーメソッドです。
   */
  private void assertRecipeDetail(Recipe recipe, String name, String imagePath, String
      recipeSource, String servings, String remark, Boolean favorite, LocalDateTime
      createdAt, LocalDateTime updatedAt) {
    assertAll(
        "Multiple assertions",
        () -> assertThat(recipe.getName(), is(name)),
        () -> assertThat(recipe.getImagePath(), is(imagePath)),
        () -> assertThat(recipe.getRecipeSource(), is(recipeSource)),
        () -> assertThat(recipe.getServings(), is(servings)),
        () -> assertThat(recipe.getRemark(), is(remark)),
        () -> assertThat(recipe.getFavorite(), is(favorite)),
        () -> assertThat(recipe.getCreatedAt(), is(createdAt)),
        () -> assertThat(recipe.getUpdatedAt(), is(updatedAt))
    );
  }

  /**
   * 材料のアサーションを行うヘルパーメソッドです。
   */
  private void assertIngredientDetail(Ingredient ingredient, int recipeId, String name,
      BigDecimal quantity, String
      unit, Boolean arrange) {
    assertAll(
        "Multiple assertions",
        () -> assertThat(ingredient.getRecipeId(), is(recipeId)),
        () -> assertThat(ingredient.getName(), is(name)),
        () -> assertThat(ingredient.getQuantity(), is(quantity)),
        () -> assertThat(ingredient.getUnit(), is(unit)),
        () -> assertThat(ingredient.getArrange(), is(arrange))
    );
  }

  /**
   * 調理手順のアサーションを行うヘルパーメソッドです。
   */
  private void assertInstructionDetail(Instruction instruction, int recipeId, int stepNumber,
      String content,
      Boolean arrange) {
    assertAll(
        "Multiple assertions",
        () -> assertThat(instruction.getRecipeId(), is(recipeId)),
        () -> assertThat(instruction.getStepNumber(), is(stepNumber)),
        () -> assertThat(instruction.getContent(), is(content)),
        () -> assertThat(instruction.getArrange(), is(arrange))
    );
  }

  /**
   * テスト用のサンプルレシピです。
   */
  private static Recipe createSampleRecipe() {
    Recipe recipe = new Recipe();
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
    ingredient1.setQuantity(BigDecimal.valueOf(1));
    ingredient1.setUnit("個");
    ingredient1.setArrange(false);

    Ingredient ingredient2 = new Ingredient();
    ingredient2.setName("水");
    ingredient2.setQuantity(null);
    ingredient2.setUnit(null);
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
