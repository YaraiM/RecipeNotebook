package raisetech.RecipeNotebook.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
  void レシピの全件検索が行えること() {
    List<Recipe> actual = sut.getAllRecipes();

    assertThat(actual.size(), is(2));
    assertRecipeDetails(actual.get(0), "卵焼き", "test1/path", "https://------1.com", "2人分",
        "備考欄1", false, LocalDateTime.parse("2024-09-22T17:00:00"),
        LocalDateTime.parse("2024-10-22T17:00:00"));
    assertRecipeDetails(actual.get(1), "目玉焼き", "test2/path", "https://------2.com", "1人分",
        "備考欄2", true, LocalDateTime.parse("2024-09-23T17:00:00"),
        LocalDateTime.parse("2024-10-23T17:00:00"));
  }

  /**
   * レシピのアサーションを行うヘルパーメソッドです。
   */
  private void assertRecipeDetails(Recipe recipe, String name, String imagePath, String
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

  @Test
  void 材料の全件検索が行えること() {
    List<Ingredient> actual = sut.getAllIngredients();

    assertThat(actual.size(), is(7));
    assertIngredientDetails(actual.get(0), 1, "卵", BigDecimal.valueOf(3.0), "個", false);
    assertIngredientDetails(actual.get(1), 1, "サラダ油", null, null, false);
    assertIngredientDetails(actual.get(2), 1, "醤油", BigDecimal.valueOf(0.5), "大さじ", false);
    assertIngredientDetails(actual.get(3), 1, "砂糖", BigDecimal.valueOf(1.0), "大さじ", false);
    assertIngredientDetails(actual.get(4), 2, "卵", BigDecimal.valueOf(1.0), "個", false);
    assertIngredientDetails(actual.get(5), 2, "サラダ油", null, null, false);
    assertIngredientDetails(actual.get(6), 2, "水", null, null, false);

  }

  /**
   * 材料のアサーションを行うヘルパーメソッドです。
   */
  private void assertIngredientDetails(Ingredient ingredient, int recipeId, String name,
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

  @Test
  void 調理手順の全件検索が行えること() {
    List<Instruction> actual = sut.getAllInstructions();

    assertThat(actual.size(), is(7));
    assertInstructionDetails(actual.get(0), 1, 1, "卵を溶いて調味料を混ぜ、卵液を作る", false);
    assertInstructionDetails(actual.get(1), 1, 2, "フライパンに油をたらし、火にかける", false);
    assertInstructionDetails(actual.get(2), 1, 3, "卵液を1/3くらいフライパンに入れて焼き、巻く",
        true);
    assertInstructionDetails(actual.get(3), 1, 4, "3の手順を繰り返して完成", false);
    assertInstructionDetails(actual.get(4), 2, 1, "フライパンに油をたらし、火にかける", false);
    assertInstructionDetails(actual.get(5), 2, 2, "フライパンに卵を割り入れる", false);
    assertInstructionDetails(actual.get(6), 2, 3,
        "少し焼けたら水を入れ、ふたをして5分、弱火にかけて完成", false);

  }

  /**
   * 調理手順のアサーションを行うヘルパーメソッドです。
   */
  private void assertInstructionDetails(Instruction instruction, int recipeId, int stepNumber,
      String content,
      Boolean arrange) {
    assertAll(
        "Multiple assertions",
        () -> assertThat(instruction.getRecipeId(), is(recipeId)),
        () -> assertThat(instruction.getStepNumber(), is(stepNumber)),
        () -> assertThat(instruction.getInstruction(), is(content)),
        () -> assertThat(instruction.getArrange(), is(arrange))
    );

  }

}
