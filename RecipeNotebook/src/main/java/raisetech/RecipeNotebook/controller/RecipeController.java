package raisetech.RecipeNotebook.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
import raisetech.RecipeNotebook.exception.ErrorResponse;
import raisetech.RecipeNotebook.exception.InvalidJsonFormatException;
import raisetech.RecipeNotebook.exception.RecipeIdMismatchException;
import raisetech.RecipeNotebook.service.RecipeService;

/**
 * レシピのCRUD処理を行うREST APIとして実行されるControllerです。
 */
@RestController
@RequestMapping("/recipes")
@Validated
public class RecipeController {

  private final RecipeService recipeService;

  @Autowired
  public RecipeController(RecipeService recipeService) {
    this.recipeService = recipeService;
  }

  @Value("${app.upload.dir}")
  private String uploadDir;

  /**
   * アプリケーション起動時にアップロードディレクトリを作成します。
   */
  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(Paths.get(uploadDir));
    } catch (IOException e) {
      throw new RuntimeException("Could not create upload directory!", e);
    }
  }

  /**
   * レシピ詳細情報の一覧検索です。RecipeSearchCriteriaで定義するリクエストパラメータを指定することで条件検索を行えます。
   *
   * @param criteria レシピ詳細情報の検索条件
   * @return レスポンス（ステータスコード200（OK）およびレシピ詳細情報一覧）
   */
  @Operation(summary = "レシピ詳細情報の一覧検索", description = "RecipeSearchCriteriaで定義するリクエストパラメータを指定することで条件検索を行えます。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = RecipeDetail.class))
          )
      ),
      @ApiResponse(responseCode = "400", description = "無効な検索条件を指定した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = ErrorResponse.class))
          )
      )
  })
  @GetMapping
  public ResponseEntity<List<RecipeDetail>> searchRecipeDetails(
      @Valid @ModelAttribute RecipeSearchCriteria criteria) {
    List<RecipeDetail> recipeDetails = recipeService.searchRecipeList(criteria);
    return ResponseEntity.ok(recipeDetails);
  }

  /**
   * レシピ詳細情報の取得です。レシピIDに紐づくレシピ詳細情報を取得します。
   *
   * @param id　レシピID
   * @return レスポンス（ステータスコード200（OK）およびレシピ詳細情報）
   */
  @Operation(summary = "レシピ詳細情報の取得", description = "レシピIDに紐づくレシピ詳細情報を取得します。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RecipeDetail.class))
      ),
      @ApiResponse(responseCode = "404", description = "存在しないIDを指定した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("/{id}")
  public ResponseEntity<RecipeDetail> getRecipeDetail(@PathVariable int id) {
    RecipeDetail recipeDetail = recipeService.searchRecipeDetail(id);
    return ResponseEntity.ok(recipeDetail);
  }

  /**
   * レシピ詳細情報の新規作成です。レシピ詳細情報およびパスを新規作成します。
   *
   * @param inputRecipeDetailJson 新規作成するレシピ詳細情報(JSON形式)
   * @param file レシピ画像ファイル
   * @param uriBuilder 新規作成時に作成されるURIのビルダー
   * @return レスポンス（ステータスコード201（CREATED）、新規作成されたURI、新規作成されたレシピ詳細情報）
   */
  @Operation(summary = "レシピ詳細情報の新規作成", description = "レシピ詳細情報およびパスを新規作成します。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "処理が成功した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RecipeDetail.class))
      ),
      @ApiResponse(responseCode = "400", description = "無効な入力形式の値をリクエストした場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(responseCode = "500", description = "ファイルのアップロードに失敗した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping("/new")
  public ResponseEntity<RecipeDetail> createRecipeDetail
  (@Valid @RequestParam("recipeDetail") String inputRecipeDetailJson,
      @RequestParam(value = "file", required = false) MultipartFile file,
      UriComponentsBuilder uriBuilder) {

    RecipeDetail inputRecipeDetail;
    try {
      inputRecipeDetail = new ObjectMapper().readValue(inputRecipeDetailJson, RecipeDetail.class);
    } catch (JsonProcessingException e) {
      throw new InvalidJsonFormatException("Invalid JSON format: " + e.getMessage());
    }

    RecipeDetail newRecipeDetail = recipeService.createRecipeDetail(inputRecipeDetail, file);

    URI location = uriBuilder.path("/recipes/{newRecipeId}")
        .buildAndExpand(newRecipeDetail.getRecipe().getId()).toUri();

    return ResponseEntity.created(location).body(newRecipeDetail);
  }

  /**
   * レシピ詳細情報の更新です。既存情報の変更や材料・調理手順の追加や削除を行います。
   *
   * @param id 更新するレシピのID
   * @param inputRecipeDetail 更新するレシピ詳細情報
   * @return レスポンス（ステータスコード200（OK）、更新されたレシピ詳細情報）
   */
  @Operation(summary = "レシピ詳細情報の更新", description = "既存情報の変更や材料・調理手順の追加や削除を行います。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RecipeDetail.class))
      ),
      @ApiResponse(responseCode = "400", description = "無効な入力形式の値をリクエストした場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(responseCode = "404", description = "存在しないIDを指定した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PutMapping("/{id}/update")
  public ResponseEntity<RecipeDetail> updateRecipeDetail
  (@PathVariable int id, @Valid @RequestBody RecipeDetail inputRecipeDetail) {

    if (inputRecipeDetail.getRecipe().getId() != id) {
      throw new RecipeIdMismatchException(
          "パスで指定したID「" + id + "」と更新対象のレシピのID「" + inputRecipeDetail.getRecipe()
              .getId()
              + "」は一致させてください");
    }

    RecipeDetail updatedRecipeDetail = recipeService.updateRecipeDetail(inputRecipeDetail);
    return ResponseEntity.ok(updatedRecipeDetail);
  }

  //TODO:API仕様書の追加

  /**
   * レシピのお気に入りフラグの更新です。指定したレシピIDに紐づくお気に入りフラグを更新します。
   *
   * @param id レシピID
   * @param request JSONをMapに変換したお気に入りフラグ（"favorite": true/false)
   * @return レスポンス（ステータスコード200（OK）、更新成功のメッセージ）
   */
  @PutMapping("/{id}/favorite")
  public ResponseEntity<String> updateFavoriteStatus(@PathVariable int id,
      @RequestBody Map<String, Boolean> request) {
    Boolean favorite = request.get("favorite");
    recipeService.updateFavoriteStatus(id, favorite);
    return ResponseEntity.ok("お気に入りを変更しました");
  }

  /**
   * レシピ詳細情報の削除です。指定したレシピIDに紐づくレシピ詳細情報を削除します。
   *
   * @param id 削除するレシピのID
   * @return レスポンス（ステータスコード200（OK）、削除成功のメッセージ）
   */
  @Operation(summary = "レシピ詳細情報の削除", description = "指定したレシピIDに紐づくレシピ詳細情報を削除します。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンス",
          content = @Content(mediaType = "text/plain",
              schema = @Schema(type = "string", example = "レシピを削除しました"))
      ),
      @ApiResponse(responseCode = "404", description = "存在しないIDを指定した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @DeleteMapping("/{id}/delete")
  public ResponseEntity<String> deleteRecipeDetail(@PathVariable int id) {
    recipeService.deleteRecipe(id);
    return ResponseEntity.ok("レシピを削除しました");
  }

}

//TODO:③画面の作成
//TODO:④ログイン機能　⇒　ユーザーのデータベースが必要。ユーザーとレシピは１対多の関係とし、レシピにユーザーIDを追加する。
//TODO:⑤タグ付け・タグ検索機能
//TODO:⑥栄養素の計算補助機能
