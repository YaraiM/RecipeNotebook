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

}
