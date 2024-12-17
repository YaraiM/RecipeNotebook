package raisetech.RecipeNotebook.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeDetailWithImageData;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
import raisetech.RecipeNotebook.exception.NullOrEmptyObjectException;
import raisetech.RecipeNotebook.exception.RecipeIdMismatchException;
import raisetech.RecipeNotebook.openapi.RecipeApiRequests.CreateRecipeRequest;
import raisetech.RecipeNotebook.openapi.RecipeApiRequests.UpdateFavoriteStatusRequest;
import raisetech.RecipeNotebook.openapi.RecipeApiRequests.UpdateRecipeRequest;
import raisetech.RecipeNotebook.openapi.RecipeApiResponses.CreateRecipeResponses;
import raisetech.RecipeNotebook.openapi.RecipeApiResponses.DeleteRecipeResponses;
import raisetech.RecipeNotebook.openapi.RecipeApiResponses.GetRecipeDetailResponses;
import raisetech.RecipeNotebook.openapi.RecipeApiResponses.SearchRecipesResponses;
import raisetech.RecipeNotebook.openapi.RecipeApiResponses.UpdateFavoriteStatusResponses;
import raisetech.RecipeNotebook.openapi.RecipeApiResponses.UpdateRecipeResponses;
import raisetech.RecipeNotebook.service.RecipeService;

/**
 * レシピのCRUD処理を行うREST APIとして実行されるControllerです。
 */
@RestController
@RequestMapping("/api/recipes")
@Validated
public class RecipeApiController {

  private final RecipeService recipeService;

  @Autowired
  public RecipeApiController(RecipeService recipeService) {
    this.recipeService = recipeService;
  }

  @Operation(
      summary = "レシピの一覧検索",
      description = "RecipeSearchCriteriaで定義するリクエストパラメータに応じたレシピ検索を行います。リクエストパラメータが全てnullの場合は全件検索を行います。")
  @SearchRecipesResponses
  @GetMapping
  public ResponseEntity<List<RecipeDetail>> searchRecipes(
      @Valid @ModelAttribute RecipeSearchCriteria recipeSearchCriteria) {
    return ResponseEntity.ok(recipeService.searchRecipeList(recipeSearchCriteria));
  }

  @Operation(
      summary = "レシピ詳細情報の取得",
      description = "指定したIDのレシピの詳細情報を取得します。"
  )
  @GetRecipeDetailResponses
  @GetMapping("/{id}")
  public ResponseEntity<RecipeDetail> getRecipeDetail(@PathVariable int id) {
    return ResponseEntity.ok(recipeService.searchRecipeDetail(id));
  }

  @Operation(
      summary = "レシピの新規作成",
      description = "入力した情報に基づきレシピを新規作成するとともに、そのレシピへのパスを作成します。"
  )
  @CreateRecipeRequest
  @CreateRecipeResponses
  @PostMapping
  public ResponseEntity<RecipeDetail> createRecipe
      (@Valid @RequestBody RecipeDetailWithImageData inputRecipeDetailWithImageData,
          UriComponentsBuilder uriBuilder) {

    RecipeDetail inputRecipeDetail = inputRecipeDetailWithImageData.getRecipeDetail();
    validateRecipeDetail(inputRecipeDetail);

    MultipartFile file = inputRecipeDetailWithImageData.convertBase64ToMultipartFile();
    RecipeDetail newRecipeDetail = recipeService.createRecipeDetail(inputRecipeDetail, file);

    URI location = uriBuilder.path("/recipes/{newRecipeId}")
        .buildAndExpand(newRecipeDetail.getRecipe().getId()).toUri();

    return ResponseEntity.created(location).body(newRecipeDetail);
  }

  @Operation(
      summary = "レシピの更新",
      description = "既存情報の変更や材料・調理手順の追加・削除を行います。"
  )
  @UpdateRecipeRequest
  @UpdateRecipeResponses
  @PatchMapping("/{id}")
  public ResponseEntity<RecipeDetail> updateRecipeDetail
      (@PathVariable int id,
          @Valid @RequestBody RecipeDetailWithImageData inputRecipeDetailWithImageData) {

    RecipeDetail inputRecipeDetail = inputRecipeDetailWithImageData.getRecipeDetail();
    validateRecipeDetail(inputRecipeDetail);
    validateRecipeId(id, inputRecipeDetail.getRecipe().getId());

    MultipartFile file = inputRecipeDetailWithImageData.convertBase64ToMultipartFile();
    RecipeDetail updatedRecipeDetail = recipeService.updateRecipeDetail(inputRecipeDetail, file);

    return ResponseEntity.ok(updatedRecipeDetail);
  }

  @Operation(
      summary = "レシピのお気に入りフラグの更新",
      description = "指定したIDに紐づくレシピのお気に入りフラグ（favorite）の更新を行います。"
  )
  @UpdateFavoriteStatusRequest
  @UpdateFavoriteStatusResponses
  @PatchMapping("/{id}/favorite")
  public ResponseEntity<String> updateFavoriteStatus(@PathVariable int id,
      @RequestBody Map<String, Boolean> request) {
    recipeService.updateFavoriteStatus(id, request.get("favorite"));
    return ResponseEntity.ok("お気に入りを変更しました");
  }

  @Operation(
      summary = "レシピの削除",
      description = "指定したIDに紐づくレシピをデータベースから削除します。レシピIDに紐づく材料および調理手順もデータベースから削除します。"
  )
  @DeleteRecipeResponses
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteRecipeDetail(@PathVariable int id) {
    recipeService.deleteRecipe(id);
    return ResponseEntity.ok("レシピを削除しました");
  }

  /**
   * 入力されたレシピ詳細情報の検証を行うメソッドです。
   *
   * @param inputRecipeDetail 入力されたレシピ詳細情報
   */
  private void validateRecipeDetail(RecipeDetail inputRecipeDetail) {
    if (inputRecipeDetail.getRecipe() == null) {
      throw new NullOrEmptyObjectException("レシピの入力は必須です");
    }
    if (inputRecipeDetail.getIngredients() == null || inputRecipeDetail.getIngredients()
        .isEmpty()) {
      throw new NullOrEmptyObjectException("材料の入力は必須です");
    }
    if (inputRecipeDetail.getInstructions() == null || inputRecipeDetail.getInstructions()
        .isEmpty()) {
      throw new NullOrEmptyObjectException("調理手順の入力は必須です");
    }
  }

  /**
   * IDの一致をチェックするメソッドです。
   *
   * @param pathId パスのID
   * @param recipeId レシピID
   */
  private void validateRecipeId(int pathId, int recipeId) {
    if (pathId != recipeId) {
      throw new RecipeIdMismatchException(
          String.format(
              "パスで指定したID「%d」と更新対象のレシピのID「%d」は一致させてください", pathId,
              recipeId));
    }
  }

}
