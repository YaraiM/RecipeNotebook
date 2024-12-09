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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeDetailWithImageData;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
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
    List<RecipeDetail> recipeDetails = recipeService.searchRecipeList(recipeSearchCriteria);
    return ResponseEntity.ok(recipeDetails);
  }

  @Operation(
      summary = "レシピ詳細情報の取得",
      description = "指定したIDのレシピの詳細情報を取得します。"
  )
  @GetRecipeDetailResponses
  @GetMapping("/{id}")
  public ResponseEntity<RecipeDetail> getRecipeDetail(@PathVariable int id) {
    RecipeDetail recipeDetail = recipeService.searchRecipeDetail(id);
    return ResponseEntity.ok(recipeDetail);
  }

  @Operation(
      summary = "レシピの新規作成",
      description = "入力した情報に基づきレシピを新規作成するとともに、そのレシピへのパスを作成します。"
  )
  @CreateRecipeRequest
  @CreateRecipeResponses
  @PostMapping("/new")
  public ResponseEntity<RecipeDetail> createRecipe
      (@Valid @RequestBody RecipeDetailWithImageData inputRecipeDetailWithImageData,
          UriComponentsBuilder uriBuilder) {

    MultipartFile file = inputRecipeDetailWithImageData.convertBase64ToMultipartFile();
    RecipeDetail inputRecipeDetail = inputRecipeDetailWithImageData.getRecipeDetail();

    RecipeDetail newRecipeDetail = recipeService.createRecipeDetail(inputRecipeDetail, file);

    URI location = uriBuilder.path("/recipes/{newRecipeId}")
        .buildAndExpand(newRecipeDetail.getRecipe().getId()).toUri();

    return ResponseEntity.created(location).

        body(newRecipeDetail);
  }

  @Operation(
      summary = "レシピの更新",
      description = "既存情報の変更や材料・調理手順の追加・削除を行います。"
  )
  @UpdateRecipeRequest
  @UpdateRecipeResponses
  @PutMapping("/{id}/update")
  public ResponseEntity<RecipeDetail> updateRecipeDetail
      (@PathVariable int id,
          @Valid @RequestBody RecipeDetailWithImageData inputRecipeDetailWithImageData) {

    if (inputRecipeDetailWithImageData.getRecipeDetail().getRecipe().getId() != id) {
      throw new RecipeIdMismatchException(
          "パスで指定したID「" + id + "」と更新対象のレシピのID「"
          + inputRecipeDetailWithImageData.getRecipeDetail().getRecipe()
              .getId()
          + "」は一致させてください");
    }

    MultipartFile file = inputRecipeDetailWithImageData.convertBase64ToMultipartFile();
    RecipeDetail inputRecipeDetail = inputRecipeDetailWithImageData.getRecipeDetail();

    RecipeDetail updatedRecipeDetail = recipeService.updateRecipeDetail(inputRecipeDetail, file);
    return ResponseEntity.ok(updatedRecipeDetail);
  }

  @Operation(
      summary = "レシピのお気に入りフラグの更新",
      description = "指定したIDに紐づくレシピのお気に入りフラグ（favorite）の更新を行います。"
  )
  @UpdateFavoriteStatusRequest
  @UpdateFavoriteStatusResponses
  @PutMapping("/{id}/favorite")
  public ResponseEntity<String> updateFavoriteStatus(@PathVariable int id,
      @RequestBody Map<String, Boolean> request) {
    Boolean favorite = request.get("favorite");
    recipeService.updateFavoriteStatus(id, favorite);
    return ResponseEntity.ok("お気に入りを変更しました");
  }

  @Operation(
      summary = "レシピの削除",
      description = "指定したIDに紐づくレシピをデータベースから削除します。レシピIDに紐づく材料および調理手順もデータベースから削除します。"
  )
  @DeleteRecipeResponses
  @DeleteMapping("/{id}/delete")
  public ResponseEntity<String> deleteRecipeDetail(@PathVariable int id) {
    recipeService.deleteRecipe(id);
    return ResponseEntity.ok("レシピを削除しました");
  }

}
