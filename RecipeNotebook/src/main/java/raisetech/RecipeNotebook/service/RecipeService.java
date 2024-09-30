package raisetech.RecipeNotebook.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.RecipeNotebook.converter.RecipeConverter;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.exception.ResourceNotFoundException;
import raisetech.RecipeNotebook.repository.RecipeRepository;

/**
 * レシピ情報を取り扱うサービスです。レシピの検索・登録・更新・削除のビジネスロジックを組んでいます。
 */
@Service
public class RecipeService {

  private final RecipeRepository repository;
  private final RecipeConverter recipeConverter;

  public RecipeService(RecipeRepository repository, RecipeConverter recipeConverter) {
    this.repository = repository;
    this.recipeConverter = recipeConverter;
  }

  /**
   * レシピ詳細情報の一覧検索です。レシピ一覧・材料一覧・調理手順一覧をconverterでレシピ詳細情報一覧に変換します。
   * TODO: 現在は全件検索のみ。絞り込みフィルターは後で実装する。
   * @return レシピ詳細情報の一覧
   */
  public List<RecipeDetail> searchRecipeList() {
    List<Recipe> allRecipes = repository.getAllRecipes();
    List<Ingredient> allIngredients = repository.getAllIngredients();
    List<Instruction> allInstructions = repository.getAllInstructions();

    return recipeConverter.convertRecipeDetails(allRecipes, allIngredients, allInstructions);

  }

  /**
   * レシピ検索です。IDに紐づくレシピを取得した後、そのレシピIDに紐づく材料と調理手順を取得し、レシピの詳細情報に変換します。
   *
   * @param id レシピのID
   * @return IDに紐づくレシピの詳細情報
   */
  public RecipeDetail searchRecipe(int id) {
    Recipe recipe = repository.getRecipe(id);

    if (recipe == null) {
      throw new ResourceNotFoundException("レシピID「" + id + "」は存在しません");
    }

    List<Ingredient> ingredients = repository.getIngredients(id);
    List<Instruction> instructions = repository.getInstructions(id);

    return new RecipeDetail(recipe, ingredients, instructions);

  }

  /**
   * レシピの新規登録です。引数として渡されたレシピ詳細情報オブジェクトに基づいて新規登録を行います。
   * 登録日時、材料および調理手順に紐づくレシピID、調理手順の番号は自動でと登録されます。
   *
   * @param recipeDetail 初期情報を除くレシピの詳細情報
   * @return 初期情報を含むレシピの詳細情報
   */
  @Transactional
  public RecipeDetail registerRecipeDetail(RecipeDetail recipeDetail) {
    Recipe recipe = recipeDetail.getRecipe();
    recipe.setCreatedAt(LocalDateTime.now());
    repository.registerRecipe(recipe);

    List<Ingredient> ingredients = recipeDetail.getIngredients();
    for (Ingredient ingredient : ingredients) {
      ingredient.setRecipeId(recipe.getId());
      repository.registerIngredient(ingredient);
    }

    List<Instruction> instructions = recipeDetail.getInstructions();
    for (int i = 0; i < instructions.size(); i++) {
      Instruction instruction = instructions.get(i);
      instruction.setRecipeId(recipe.getId());
      instruction.setStepNumber(i + 1);
      repository.registerInstruction(instruction);
    }

    return recipeDetail;
  }

  @Transactional
  public RecipeDetail updateRecipeDetail(RecipeDetail recipeDetail) {
    int recipeId = recipeDetail.getRecipe().getId();
    if (repository.getRecipe(recipeId) == null) {
      throw new ResourceNotFoundException("レシピID「" + recipeId + "」は存在しません");
    }

    for (Ingredient ingredient : recipeDetail.getIngredients()) {
      int ingredientId = ingredient.getId();
      if (repository.getIngredient(ingredientId) == null) {
        throw new ResourceNotFoundException("材料ID「" + ingredientId + "」は存在しません");
      }
    }

    for (Instruction instruction : recipeDetail.getInstructions()) {
      int instructionId = instruction.getId();
      if (repository.getInstruction(instructionId) == null) {
        throw new ResourceNotFoundException("調理手順ID「" + instructionId + "」は存在しません");
      }
    }

    Recipe recipe = recipeDetail.getRecipe();
    recipe.setUpdatedAt(LocalDateTime.now());
    repository.updateRecipe(recipe);

    recipeDetail.getIngredients().forEach(repository::updateIngredient);

    recipeDetail.getInstructions().forEach(repository::updateInstruction);

    return recipeDetail;
  }

}
