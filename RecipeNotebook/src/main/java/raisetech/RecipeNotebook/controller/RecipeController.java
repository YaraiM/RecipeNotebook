package raisetech.RecipeNotebook.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
import org.springframework.web.util.UriComponentsBuilder;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
import raisetech.RecipeNotebook.exception.RecipeIdMismatchException;
import raisetech.RecipeNotebook.service.RecipeService;

@RestController
@RequestMapping("/recipes")
@Validated
public class RecipeController {

  private final RecipeService service;

  @Autowired
  public RecipeController(RecipeService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<RecipeDetail>> getRecipes(
      @Valid @ModelAttribute RecipeSearchCriteria criteria) {
    List<RecipeDetail> recipeDetails = service.searchRecipeList(criteria);
    return ResponseEntity.ok(recipeDetails);
  }

  @GetMapping("/{id}")
  public ResponseEntity<RecipeDetail> getRecipeDetail(@PathVariable int id) {
    RecipeDetail recipeDetail = service.searchRecipeDetail(id);
    return ResponseEntity.ok(recipeDetail);
  }

  @PostMapping("/new")
  public ResponseEntity<RecipeDetail> registerRecipeDetail
      (@Valid @RequestBody RecipeDetail recipeDetail, UriComponentsBuilder uriBuilder) {
    RecipeDetail newRecipeDetail = service.registerRecipeDetail(recipeDetail);

    int newRecipeId = newRecipeDetail.getRecipe().getId();
    URI location = uriBuilder.path("/recipes/{newRecipeId}").buildAndExpand(newRecipeId).toUri();

    return ResponseEntity.created(location).body(newRecipeDetail);
  }

  @PutMapping("/{id}/update")
  public ResponseEntity<RecipeDetail> updateRecipeDetail
      (@PathVariable int id, @Valid @RequestBody RecipeDetail recipeDetail) {

    if (recipeDetail.getRecipe().getId() != id) {
      throw new RecipeIdMismatchException(
          "パスで指定したID「" + id + "」と更新対象のレシピのID「" + recipeDetail.getRecipe().getId()
              + "」は一致させてください");
    }

    RecipeDetail updatedRecipeDetail = service.updateRecipeDetail(recipeDetail);
    return ResponseEntity.ok(updatedRecipeDetail);
  }

  @DeleteMapping("/{id}/delete")
  public ResponseEntity<String> deleteRecipeDetail(@PathVariable int id) {
    service.deleteRecipe(id);
    return ResponseEntity.ok("レシピを削除しました");
  }

}

//TODO:⓪controllerのPRをマージ
//TODO:①Javadoc,API仕様書の実装
//TODO:②結合テストの実施
//TODO:③画面の作成
//TODO:④ログイン機能　⇒　ユーザーのデータベースが必要。ユーザーとレシピは１対多の関係とし、レシピにユーザーIDを追加する。
//TODO:⑤タグ付け・タグ検索機能
//TODO:⑥栄養素の計算補助機能
