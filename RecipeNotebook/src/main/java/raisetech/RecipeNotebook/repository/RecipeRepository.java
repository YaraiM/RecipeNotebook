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

//  TODO:レシピの全件検索時は食材や手順の情報を統合した情報を検索したいので、食材や手順の全件検索も必要

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

  /**
   * レシピの新規登録です。新規レシピをレシピテーブルに追加します。
   *
   * @param recipe レシピ
   */
  void registerRecipe(Recipe recipe);

  /**
   * 材料の新規登録です。レシピに使用される材料を材料テーブルに追加します。
   *
   * @param ingredient レシピの材料
   */
  void registerIngredient(Ingredient ingredient);

  /**
   * 手順の新規登録です。レシピの作成手順を作成手順テーブルに追加します。
   *
   * @param instruction レシピの作成手順
   */
  void registerInstruction(Instruction instruction);

  /**
   * レシピの更新です。レシピ情報の中にあるIDを参照して、レシピテーブルで該当するレコードを更新します。
   *
   * @param recipe レシピ
   */
  void updateRecipe(Recipe recipe);

  /**
   * レシピの材料の更新です。レシピの材料情報の中にあるレシピIDを参照して、材料テーブルで該当するレコードを更新します。
   *
   * @param ingredient レシピの材料
   */
  void updateIngredient(Ingredient ingredient);

  /**
   * レシピの作成手順の更新です。レシピの作成手順情報の中にあるレシピIDを参照して、作成手順テーブルで該当するレコードを更新します。
   *
   * @param instruction レシピの作成手順
   */
  void updateInstruction(Instruction instruction);

}
