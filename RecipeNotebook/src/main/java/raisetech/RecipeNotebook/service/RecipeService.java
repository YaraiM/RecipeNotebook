package raisetech.RecipeNotebook.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
import raisetech.RecipeNotebook.exception.ResourceNotFoundException;
import raisetech.RecipeNotebook.repository.RecipeRepository;

/**
 * レシピ情報を取り扱うサービスです。レシピの検索・登録・更新・削除のビジネスロジックを組んでいます。
 */
@Service
public class RecipeService {

  private final RecipeRepository repository;

  @Autowired
  public RecipeService(RecipeRepository repository) {
    this.repository = repository;
  }

  /**
   * 検索条件に応じてレシピ詳細情報を一覧検索します。
   * @return レシピ詳細情報の一覧
   */
  public List<RecipeDetail> searchRecipeList(RecipeSearchCriteria criteria) {
    // レシピを検索
    List<Recipe> recipes = repository.getRecipes(criteria);
    if (recipes.isEmpty()) {
      return Collections.emptyList();
    }

    List<Integer> recipeIds = recipes.stream()
        .map(Recipe::getId).collect(Collectors.toList());

    // 材料名での検索
    List<Integer> recipeIdsWithMatchingIngredients =
        repository.getRecipeIdsWithMatchingIngredients(recipeIds, criteria.getIngredientNames());

    recipeIds = recipeIds.stream()
        .filter(recipeIdsWithMatchingIngredients::contains)
        .collect(Collectors.toList());

    if (recipeIds.isEmpty()) {
      return Collections.emptyList();
    }

    return recipeIds.stream()
        .map(this::searchRecipeDetail)
        .collect(Collectors.toList());

  }

  /**
   * レシピ検索です。IDに紐づくレシピを取得した後、そのレシピIDに紐づく材料と調理手順を取得し、レシピの詳細情報に変換します。
   *
   * @param id レシピのID
   * @return IDに紐づくレシピの詳細情報
   */
  public RecipeDetail searchRecipeDetail(int id) {
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
   * 登録日時、材料および調理手順に紐づくレシピID、調理手順の番号は自動で登録されます。
   *
   * @param recipeDetail 初期情報を除くレシピの詳細情報
   * @return 新規登録されたレシピ詳細情報
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

  /**
   * レシピの更新です。引数で渡されたレシピ詳細情報のレシピID・材料ID・調理手順IDにそれぞれ紐づく情報を更新します。
   *
   * @param inputRecipeDetail レシピ詳細情報
   * @return 更新されたレシピ詳細情報
   */
  @Transactional
  public RecipeDetail updateRecipeDetail(RecipeDetail inputRecipeDetail) {
    int recipeId = inputRecipeDetail.getRecipe().getId();
    if (repository.getRecipe(recipeId) == null) {
      throw new ResourceNotFoundException("レシピID「" + recipeId + "」は存在しません");
    }

    // 既存のデータを取得
    List<Ingredient> existingIngredients = repository.getIngredients(recipeId);
    List<Instruction> existingInstructions = repository.getInstructions(recipeId);

    // 材料の更新
    Set<Integer> inputIngredientIds = inputRecipeDetail.getIngredients().stream()
        .map(Ingredient::getId)
        .collect(Collectors.toSet());

    // 材料の削除処理
    existingIngredients.stream()
        .filter(ingredient -> !inputIngredientIds.contains(ingredient.getId()))
        .forEach(ingredient -> repository.deleteIngredient(ingredient.getId()));

    // 材料の追加・更新処理
    inputRecipeDetail.getIngredients().forEach(ingredient -> {
      if (ingredient.getId() == 0) {
        repository.registerIngredient(ingredient);
      } else {
        if (repository.getIngredient(ingredient.getId()) == null) {
          throw new ResourceNotFoundException("材料ID「" + ingredient.getId() + "」は存在しません");
        }
        repository.updateIngredient(ingredient);
      }
    });

    // 調理手順の更新
    Set<Integer> inputInstructionIds = inputRecipeDetail.getInstructions().stream()
        .map(Instruction::getId)
        .collect(Collectors.toSet());

    // 調理手順の削除処理
    existingInstructions.stream()
        .filter(instruction -> !inputInstructionIds.contains(instruction.getId()))
        .forEach(instruction -> repository.deleteInstruction(instruction.getId()));

    // 調理手順の追加・更新処理
    inputRecipeDetail.getInstructions().forEach(instruction -> {
      if (instruction.getId() == 0) {
        repository.registerInstruction(instruction);
      } else {
        if (repository.getInstruction(instruction.getId()) == null) {
          throw new ResourceNotFoundException(
              "調理手順ID「" + instruction.getId() + "」は存在しません");
        }
        repository.updateInstruction(instruction);
      }
    });

    // レシピ本体の更新
    Recipe inputRecipe = inputRecipeDetail.getRecipe();
    inputRecipe.setUpdatedAt(LocalDateTime.now());
    repository.updateRecipe(inputRecipe);

    return inputRecipeDetail;
  }

  /**
   * レシピを削除するメソッドです。データベース側の設定により、レシピIDに紐づく材料と調理手順も削除されます。
   * したがって、レシピ詳細情報を削除することを意味します。
   *
   * @param id レシピID
   */
  @Transactional
  public void deleteRecipe(int id) {

    if (repository.getRecipe(id) == null) {
      throw new ResourceNotFoundException("レシピID「" + id + "」は存在しません");
    }

    repository.deleteRecipe(id);

  }

  /**
   * 材料を削除するメソッドです。レシピ更新の際、レシピ詳細情報に含まれる特定の材料を削除することを想定しています。
   *
   * @param id 材料ID
   */
  @Transactional
  public void deleteIngredient(int id) {

    if (repository.getIngredient(id) == null) {
      throw new ResourceNotFoundException("材料ID「" + id + "」は存在しません");
    }

    repository.deleteIngredient(id);

  }

  /**
   * 調理手順を削除するメソッドです。レシピ更新の際、レシピ詳細情報に含まれる特定の調理手順を削除することを想定しています。
   *
   * @param id 調理手順ID
   */
  @Transactional
  public void deleteInstruction(int id) {

    if (repository.getInstruction(id) == null) {
      throw new ResourceNotFoundException("調理手順ID「" + id + "」は存在しません");
    }

    repository.deleteInstruction(id);

  }

}
