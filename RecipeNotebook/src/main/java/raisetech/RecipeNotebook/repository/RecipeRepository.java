package raisetech.RecipeNotebook.repository;


import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;

@Mapper
public interface RecipeRepository {

  /**
   * レシピを全件取得します。
   *
   * @return レシピ一覧（全件）
   */
  List<Recipe> getAllRecipes();

  /**
   * IDに紐づくレシピを取得します。
   *
   * @param id ID
   * @return レシピ
   */
  Recipe getRecipe(int id);

  /**
   * レシピIDに紐づく材料を取得します。
   *
   * @param recipeId レシピID
   * @return レシピの食材一覧
   */
  List<Ingredient> getIngredients(int recipeId);

  /**
   * レシピIDに紐づく手順を取得します。
   *
   * @param recipeId レシピID
   * @return レシピの手順一覧
   */
  List<Instruction> getInstructions(int recipeId);

}
