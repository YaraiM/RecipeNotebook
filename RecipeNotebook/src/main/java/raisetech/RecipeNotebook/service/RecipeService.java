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
import raisetech.RecipeNotebook.data.User;
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
  private final CustomUserDetailsService customUserDetailsService;
  private final FileStorageService fileStorageService;

  @Autowired
  public RecipeService(RecipeRepository repository,
      CustomUserDetailsService customUserDetailsService,
      FileStorageService fileStorageService) {
    this.repository = repository;
    this.customUserDetailsService = customUserDetailsService;
    this.fileStorageService = fileStorageService;
  }

  /**
   * 検索条件に応じてレシピ詳細情報一覧を取得します。
   * @return レシピ詳細情報の一覧
   */
  public List<RecipeDetail> searchRecipeList(RecipeSearchCriteria criteria) {
    User loggedInUser = customUserDetailsService.getLoggedInUser();

    // レシピを検索
    List<Recipe> recipes = repository.getRecipes(loggedInUser.getId(), criteria);
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
    validateRecipeExists(id);
    Recipe recipe = repository.getRecipe(id);
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
    User loggedInUser = customUserDetailsService.getLoggedInUser();

    Recipe inputRecipe = recipeDetail.getRecipe();
    inputRecipe.setUserId(loggedInUser.getId());
    String imagePath = fileStorageService.storeFile(file);
    inputRecipe.setImagePath(imagePath);
    inputRecipe.setCreatedAt(LocalDateTime.now());
    repository.registerRecipe(inputRecipe);

    List<Ingredient> inputIngredients = recipeDetail.getIngredients();
    for (Ingredient ingredient : inputIngredients) {
      ingredient.setRecipeId(inputRecipe.getId());
      repository.registerIngredient(ingredient);
    }

    List<Instruction> inputInstructions = recipeDetail.getInstructions();
    for (int i = 0; i < inputInstructions.size(); i++) {
      Instruction instruction = inputInstructions.get(i);
      instruction.setRecipeId(inputRecipe.getId());
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
    Recipe inputRecipe = recipeDetail.getRecipe();
    List<Ingredient> inputIngredients = recipeDetail.getIngredients();
    List<Instruction> inputInstructions = recipeDetail.getInstructions();

    int recipeId = inputRecipe.getId();
    validateRecipeExists(recipeId);
    // 入力されたレシピ詳細情報の材料および調理手順のレシピIDにレシピIDをセット
    setRecipeIdForComponents(inputIngredients, inputInstructions, recipeId);
    // 既存のデータを取得
    List<Ingredient> existingIngredients = repository.getIngredients(recipeId);
    List<Instruction> existingInstructions = repository.getInstructions(recipeId);

    updateIngredients(inputIngredients, existingIngredients);
    updateInstructions(inputInstructions, existingInstructions);
    updateRecipeWithImage(inputRecipe, recipeId, file);

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
    validateRecipeExists(id);
    repository.updateFavoriteStatus(id, favorite);
  }

  /**
   * レシピを削除するメソッドです。データベース側の設定により、レシピIDに紐づく材料と調理手順も削除されます。
   *
   * @param id レシピID
   */
  @Transactional
  public void deleteRecipe(int id) {
    validateRecipeExists(id);
    String imagePathForDeletedRecipe = repository.getRecipe(id).getImagePath();
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
    validateIngredientExists(id);
    repository.deleteIngredient(id);
  }

  /**
   * 調理手順を削除するメソッドです。レシピ更新の際、レシピ詳細情報に含まれる特定の調理手順を削除するために使用します。
   *
   * @param id 調理手順ID
   */
  @Transactional
  public void deleteInstruction(int id) {
    validateInstructionExists(id);
    repository.deleteInstruction(id);
  }

  /**
   * レシピの存在確認を行うメソッドです。
   *
   * @param recipeId レシピID
   */
  private void validateRecipeExists(int recipeId) {
    if (repository.getRecipe(recipeId) == null) {
      throw new ResourceNotFoundException("レシピID「" + recipeId + "」は存在しません");
    }
  }

  /**
   * 材料の存在確認を行うメソッドです。
   *
   * @param ingredientId 材料ID
   */
  private void validateIngredientExists(int ingredientId) {
    if (repository.getIngredient(ingredientId) == null) {
      throw new ResourceNotFoundException("材料ID「" + ingredientId + "」は存在しません");
    }
  }

  /**
   * 調理手順の存在確認を行うメソッドです。
   *
   * @param instructionId 調理手順ID
   */
  private void validateInstructionExists(int instructionId) {
    if (repository.getInstruction(instructionId) == null) {
      throw new ResourceNotFoundException("調理手順ID「" + instructionId + "」は存在しません");
    }
  }

  /**
   * 材料リストおよび調理手順リストにレシピIDをセットするメソッドです。
   *
   * @param inputIngredients 材料リスト
   * @param inputInstructions 調理手順リスト
   * @param recipeId レシピID
   */
  private void setRecipeIdForComponents(List<Ingredient> inputIngredients,
      List<Instruction> inputInstructions, int recipeId) {
    inputIngredients.forEach(ingredient -> ingredient.setRecipeId(recipeId));
    inputInstructions.forEach(instruction -> instruction.setRecipeId(recipeId));
  }

  /**
   * レシピを更新するメソッドです。
   *
   * @param inputRecipe 入力されたレシピ情報
   * @param recipeId レシピID
   * @param file 画像ファイル
   */
  private void updateRecipeWithImage(Recipe inputRecipe, int recipeId, MultipartFile file) {
    String existingImagePath = repository.getRecipe(recipeId).getImagePath();
    if (file != null && !file.isEmpty()) {
      if (!existingImagePath.contains("/images/") && existingImagePath.contains("/uploads/")) {
        fileStorageService.deleteFile(existingImagePath);
      }
      String updateImagePath = fileStorageService.storeFile(file);
      inputRecipe.setImagePath(updateImagePath);
    } else {
      inputRecipe.setImagePath(existingImagePath);
    }

    inputRecipe.setUpdatedAt(LocalDateTime.now());
    repository.updateRecipe(inputRecipe);
  }

  /**
   * 材料を更新するメソッドです。
   *
   * @param inputIngredients 入力された材料リスト
   * @param existingIngredients 既存の材料リスト
   */
  private void updateIngredients(List<Ingredient> inputIngredients,
      List<Ingredient> existingIngredients) {
    Set<Integer> inputIngredientIds = inputIngredients.stream()
        .map(Ingredient::getId)
        .collect(Collectors.toSet());
    // 材料の削除処理
    existingIngredients.stream()
        .filter(ingredient -> !inputIngredientIds.contains(ingredient.getId()))
        .forEach(ingredient -> repository.deleteIngredient(ingredient.getId()));
    // 材料の追加・更新処理
    inputIngredients.forEach(ingredient -> {
      if (ingredient.getId() == 0) {
        repository.registerIngredient(ingredient);
      } else {
        validateIngredientExists(ingredient.getId());
        repository.updateIngredient(ingredient);
      }
    });
  }

  /**
   * 調理手順を更新するメソッドです。
   *
   * @param inputInstructions 入力された調理手順リスト
   * @param existingInstructions 既存の調理手順リスト
   */
  private void updateInstructions(List<Instruction> inputInstructions,
      List<Instruction> existingInstructions) {
    Set<Integer> inputInstructionIds = inputInstructions.stream()
        .map(Instruction::getId)
        .collect(Collectors.toSet());
    // 調理手順の削除処理
    existingInstructions.stream()
        .filter(instruction -> !inputInstructionIds.contains(instruction.getId()))
        .forEach(instruction -> repository.deleteInstruction(instruction.getId()));
    // 調理手順の追加・更新処理
    inputInstructions.forEach(instruction -> {
      if (instruction.getId() == 0) {
        repository.registerInstruction(instruction);
      } else {
        validateInstructionExists(instruction.getId());
        repository.updateInstruction(instruction);
      }
    });
  }

}
