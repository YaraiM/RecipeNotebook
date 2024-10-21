package raisetech.RecipeNotebook.repository;


import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;

@Mapper
public interface RecipeRepository {

  /**
   * 検索条件に応じてレシピ一覧を取得します。条件を指定しない場合は全件取得になります。
   *
   * @param criteria レシピ検索条件
   * @return レシピ一覧
   */
  List<Recipe> getRecipes(RecipeSearchCriteria criteria);

  /**
   * IDに紐づくレシピを取得します。
   *
   * @param id ID
   * @return レシピ
   */
  Recipe getRecipe(int id);

  /**
   * 材料を全件取得します。テストに使用します。
   *
   * @return 材料一覧（全件）
   */
  List<Ingredient> getAllIngredients();

  /**
   * レシピIDに紐づく材料一覧を取得します。
   *
   * @param recipeId レシピID
   * @return レシピの材料一覧
   */
  List<Ingredient> getIngredients(int recipeId);

  /**
   * IDに紐づく材料を取得します。
   *
   * @param id 材料のID
   * @return 材料
   */
  Ingredient getIngredient(int id);

  /**
   * 調理手順を全件取得します。テストに使用します。
   *
   * @return 調理手順一覧（全件）
   */
  List<Instruction> getAllInstructions();

  /**
   * レシピIDに紐づく調理手順一覧を取得します。
   *
   * @param recipeId レシピID
   * @return レシピの調理手順一覧
   */
  List<Instruction> getInstructions(int recipeId);

  /**
   * IDに紐づく調理手順を取得します。
   *
   * @param id 調理手順のID
   * @return 調理手順
   */
  Instruction getInstruction(int id);

  /**
   * レシピID一覧に紐づき、かつ指定したキーワードに合致する材料のID一覧を取得します。
   *
   * @param recipeIds レシピID一覧
   * @param ingredientNames 材料名
   * @return 材料のID一覧
   */
  List<Integer> getRecipeIdsWithMatchingIngredients(List<Integer> recipeIds,
      List<String> ingredientNames);

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
   * 調理手順の新規登録です。レシピの調理手順を調理手順テーブルに追加します。
   *
   * @param instruction レシピの調理手順
   */
  void registerInstruction(Instruction instruction);

  /**
   * レシピの更新です。レシピ情報の中にあるIDを参照して、レシピテーブルで該当するレコードを更新します。
   *
   * @param recipe レシピ
   */
  void updateRecipe(Recipe recipe);

  /**
   * レシピの材料の更新です。材料情報の中にあるIDを参照して、材料テーブルで該当するレコードを更新します。
   *
   * @param ingredient レシピの材料
   */
  void updateIngredient(Ingredient ingredient);

  /**
   * レシピの調理手順の更新です。調理手順情報の中にあるIDを参照して、調理手順テーブルで該当するレコードを更新します。
   *
   * @param instruction レシピの調理手順
   */
  void updateInstruction(Instruction instruction);

  /**
   * レシピの削除です。レシピテーブルにおいて、指定したIDに紐づくレコードを削除します。
   * データベース側でデリートカスケードの設定をしているため、このメソッドが成功するとレシピIDに紐づく材料と調理手順がすべて削除されます。
   *
   * @param id レシピID
   */
  void deleteRecipe(int id);

  /**
   * レシピの材料の削除です。材料テーブルにおいて、指定したIDに紐づくレコードを削除します。
   *
   * @param id 材料ID
   */
  void deleteIngredient(int id);

  /**
   * レシピの調理手順の削除です。調理手順テーブルにおいて、指定したIDに紐づくレコードを削除します。
   *
   * @param id 調理手順ID
   */
  void deleteInstruction(int id);

}
