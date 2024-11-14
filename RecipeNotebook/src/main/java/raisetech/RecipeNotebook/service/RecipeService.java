package raisetech.RecipeNotebook.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
  private final FileStorageService fileStorageService;

  @Autowired
  public RecipeService(RecipeRepository repository, FileStorageService fileStorageService) {
    this.repository = repository;
    this.fileStorageService = fileStorageService;
  }

  /**
   * 検索条件に応じてレシピ詳細情報一覧を取得します。
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
   * レシピの新規作成です。引数として渡されたレシピ詳細情報に基づいて新規登録を行います。
   * 登録日時、材料および調理手順に紐づくレシピID、調理手順の番号は自動で登録されます。
   *
   * @param recipeDetail レシピの詳細情報
   * @return 新規作成されるレシピ詳細情報
   */
  @Transactional
  public RecipeDetail createRecipeDetail(RecipeDetail recipeDetail, MultipartFile file) {
    Recipe recipe = recipeDetail.getRecipe();
    String imagePath = fileStorageService.storeFile(file);
    recipeDetail.getRecipe().setImagePath(imagePath);
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
   * @param recipeDetail レシピ詳細情報
   * @return 更新されるレシピ詳細情報
   */
  @Transactional
  public RecipeDetail updateRecipeDetail(RecipeDetail recipeDetail, MultipartFile file) {
    int recipeId = recipeDetail.getRecipe().getId();
    if (repository.getRecipe(recipeId) == null) {
      throw new ResourceNotFoundException("レシピID「" + recipeId + "」は存在しません");
    }

    // 入力されたレシピ詳細情報の材料および調理手順のレシピIDにレシピIDをセット
    for (Ingredient ingredient : recipeDetail.getIngredients()) {
      ingredient.setRecipeId(recipeId);
    }
    for (Instruction instruction : recipeDetail.getInstructions()) {
      instruction.setRecipeId(recipeId);
    }

    // 既存のデータを取得
    List<Ingredient> existingIngredients = repository.getIngredients(recipeId);
    List<Instruction> existingInstructions = repository.getInstructions(recipeId);

    // 材料の更新
    Set<Integer> inputIngredientIds = recipeDetail.getIngredients().stream()
        .map(Ingredient::getId)
        .collect(Collectors.toSet());

    // 材料の削除処理
    existingIngredients.stream()
        .filter(ingredient -> !inputIngredientIds.contains(ingredient.getId()))
        .forEach(ingredient -> repository.deleteIngredient(ingredient.getId()));

    // 材料の追加・更新処理
    recipeDetail.getIngredients().forEach(ingredient -> {
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
    Set<Integer> inputInstructionIds = recipeDetail.getInstructions().stream()
        .map(Instruction::getId)
        .collect(Collectors.toSet());

    // 調理手順の削除処理
    existingInstructions.stream()
        .filter(instruction -> !inputInstructionIds.contains(instruction.getId()))
        .forEach(instruction -> repository.deleteInstruction(instruction.getId()));

    // 調理手順の追加・更新処理
    recipeDetail.getInstructions().forEach(instruction -> {
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
    Recipe inputRecipe = recipeDetail.getRecipe();

    String existingImagePath = repository.getRecipe(recipeId).getImagePath();
    if (file != null && !file.isEmpty()) {
      fileStorageService.deleteFile(existingImagePath);
      String updateImagePath = fileStorageService.storeFile(file);
      inputRecipe.setImagePath(updateImagePath);
    } else {
      inputRecipe.setImagePath(existingImagePath);
    }

    inputRecipe.setUpdatedAt(LocalDateTime.now());
    repository.updateRecipe(inputRecipe);

    return recipeDetail;
  }

  /**
   * レシピのお気に入りフラグを更新するメソッドです。
   *
   * @param id レシピID
   * @param favorite レシピのお気に入りフラグ
   */
  @Transactional
  public void updateFavoriteStatus(int id, boolean favorite) {

    if (repository.getRecipe(id) == null) {
      throw new ResourceNotFoundException("レシピID「" + id + "」は存在しません");
    }

    repository.updateFavoriteStatus(id, favorite);

  }

  /**
   * レシピを削除するメソッドです。データベース側の設定により、レシピIDに紐づく材料と調理手順も削除されます。
   *
   * @param id レシピID
   */
  @Transactional
  public void deleteRecipe(int id) {

    Recipe deletedRecipe = repository.getRecipe(id);

    if (deletedRecipe == null) {
      throw new ResourceNotFoundException("レシピID「" + id + "」は存在しません");
    }

    String imagePathForDeletedRecipe = deletedRecipe.getImagePath();
    if (imagePathForDeletedRecipe.contains("/uploads/")) {
      fileStorageService.deleteFile(imagePathForDeletedRecipe);
    }
    repository.deleteRecipe(id);
  }

  /**
   * 材料を削除するメソッドです。レシピ更新の際、レシピ詳細情報に含まれる特定の材料を削除するために使用します。
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
   * 調理手順を削除するメソッドです。レシピ更新の際、レシピ詳細情報に含まれる特定の調理手順を削除するために使用します。
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
