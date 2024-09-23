package raisetech.RecipeNotebook.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

    Recipe actual = sut.getRecipe(3);
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
      sut.registerIngredient(ingredient);
    }

    List<Ingredient> actual = sut.getIngredients(3);
    assertIngredientDetail(actual.get(0), 3, "卵", BigDecimal.valueOf(1.0), "個", false);
    assertIngredientDetail(actual.get(1), 3, "水", null, null, false);

  }

  @Test
  void 調理手順をデータベースに追加できること() {
    Recipe recipe = createSampleRecipe();
    sut.registerRecipe(recipe);

    List<Instruction> instructions = createSampleInstructions();
    for (Instruction instruction : instructions) {
      sut.registerInstruction(instruction);
    }

    List<Instruction> actual = sut.getInstructions(3);
    assertInstructionDetail(actual.get(0), 3, 1, "鍋に卵がかぶるくらいの水を入れて沸騰させる",
        false);
    assertInstructionDetail(actual.get(1), 3, 2,
        "卵を沸かした水に入れて7~12分茹でる。好みの硬さで時間を調整する", false);

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
   */
  private static List<Ingredient> createSampleIngredients() {
    List<Ingredient> ingredients = new ArrayList<>();

    Ingredient ingredient1 = new Ingredient();
    ingredient1.setRecipeId(3);
    ingredient1.setName("卵");
    ingredient1.setQuantity(BigDecimal.valueOf(1));
    ingredient1.setUnit("個");
    ingredient1.setArrange(false);

    Ingredient ingredient2 = new Ingredient();
    ingredient2.setRecipeId(3);
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
   */
  private static List<Instruction> createSampleInstructions() {
    List<Instruction> instructions = new ArrayList<>();

    Instruction instruction1 = new Instruction();
    instruction1.setRecipeId(3);
    instruction1.setStepNumber(1);
    instruction1.setContent("鍋に卵がかぶるくらいの水を入れて沸騰させる");
    instruction1.setArrange(false);

    Instruction instruction2 = new Instruction();
    instruction2.setRecipeId(3);
    instruction2.setStepNumber(2);
    instruction2.setContent("卵を沸かした水に入れて7~12分茹でる。好みの硬さで時間を調整する");
    instruction2.setArrange(false);

    instructions.add(instruction1);
    instructions.add(instruction2);

    return instructions;
  }

}
