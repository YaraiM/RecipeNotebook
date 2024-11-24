package raisetech.RecipeNotebook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import raisetech.RecipeNotebook.exception.ErrorResponse;
import raisetech.RecipeNotebook.exception.RecipeIdMismatchException;
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
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンスです。",
          content = @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = RecipeDetail.class)),
              examples = {
                  @ExampleObject(
                      summary = "検索条件に合致するレシピが1件見つかった場合",
                      value = """
                              [
                                {
                                  "recipe": {
                                    "id": 2,
                                    "name": "目玉焼き",
                                    "imagePath": "/images/medamayaki.jpg",
                                    "recipeSource": "https://------2.com",
                                    "servings": "1人前",
                                    "remark": "基本の目玉焼きのレシピです。",
                                    "favorite": true,
                                    "createdAt": "2024-09-23T17:00:00",
                                    "updatedAt": "2024-10-23T17:00:00"
                                  },
                                  "ingredients": [
                                    {
                                      "id": 5,
                                      "recipeId": 2,
                                      "name": "卵",
                                      "quantity": "1個",
                                      "arrange": false
                                    },
                                    {
                                      "id": 6,
                                      "recipeId": 2,
                                      "name": "サラダ油",
                                      "quantity": "適量",
                                      "arrange": false
                                    },
                                    {
                                      "id": 7,
                                      "recipeId": 2,
                                      "name": "水",
                                      "quantity": null,
                                      "arrange": false
                                    }
                                  ],
                                  "instructions": [
                                    {
                                      "id": 5,
                                      "recipeId": 2,
                                      "stepNumber": 1,
                                      "content": "フライパンに油をたらし、火にかける",
                                      "arrange": false
                                    },
                                    {
                                      "id": 6,
                                      "recipeId": 2,
                                      "stepNumber": 2,
                                      "content": "フライパンに卵を割り入れる",
                                      "arrange": false
                                    },
                                    {
                                      "id": 7,
                                      "recipeId": 2,
                                      "stepNumber": 3,
                                      "content": "少し焼けたら水を入れ、ふたをして5分、弱火にかけて完成",
                                      "arrange": false
                                    }
                                  ]
                                }
                              ]
                          """
                  )
              }
          )
      ),
      @ApiResponse(responseCode = "400", description = "不正な検索条件を指定した場合のレスポンスです。",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(
                      summary = "終了日が開始日より前の日付を指定した場合",
                      value = """
                                {
                                   "status": "BAD_REQUEST",
                                   "message": "バリデーションエラーです。入力フォームを確認してください",
                                   "errors": [
                                     {
                                       "field": "createDateTo",
                                       "message": "終了日が開始日より前の日付になっています"
                                     }
                                   ]
                                }
                          """
                  )
              }
          )
      )
  })
  @GetMapping
  public ResponseEntity<List<RecipeDetail>> searchRecipeDetails(
      @Valid @ModelAttribute RecipeSearchCriteria recipeSearchCriteria) {
    List<RecipeDetail> recipeDetails = recipeService.searchRecipeList(recipeSearchCriteria);
    return ResponseEntity.ok(recipeDetails);
  }

  @Operation(
      summary = "レシピ詳細情報の取得",
      description = "指定したIDのレシピの詳細情報を取得します。")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "処理が成功した場合のレスポンスです。",
          content = @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = RecipeDetail.class)),
              examples = {
                  @ExampleObject(
                      summary = "ID=2を指定した場合",
                      value = """
                              [
                                {
                                  "recipe": {
                                    "id": 2,
                                    "name": "目玉焼き",
                                    "imagePath": "/images/medamayaki.jpg",
                                    "recipeSource": "https://------2.com",
                                    "servings": "1人前",
                                    "remark": "基本の目玉焼きのレシピです。",
                                    "favorite": true,
                                    "createdAt": "2024-09-23T17:00:00",
                                    "updatedAt": "2024-10-23T17:00:00"
                                  },
                                  "ingredients": [
                                    {
                                      "id": 5,
                                      "recipeId": 2,
                                      "name": "卵",
                                      "quantity": "1個",
                                      "arrange": false
                                    },
                                    {
                                      "id": 6,
                                      "recipeId": 2,
                                      "name": "サラダ油",
                                      "quantity": "適量",
                                      "arrange": false
                                    },
                                    {
                                      "id": 7,
                                      "recipeId": 2,
                                      "name": "水",
                                      "quantity": null,
                                      "arrange": false
                                    }
                                  ],
                                  "instructions": [
                                    {
                                      "id": 5,
                                      "recipeId": 2,
                                      "stepNumber": 1,
                                      "content": "フライパンに油をたらし、火にかける",
                                      "arrange": false
                                    },
                                    {
                                      "id": 6,
                                      "recipeId": 2,
                                      "stepNumber": 2,
                                      "content": "フライパンに卵を割り入れる",
                                      "arrange": false
                                    },
                                    {
                                      "id": 7,
                                      "recipeId": 2,
                                      "stepNumber": 3,
                                      "content": "少し焼けたら水を入れ、ふたをして5分、弱火にかけて完成",
                                      "arrange": false
                                    }
                                  ]
                                }
                              ]
                          """
                  )
              }
          )
      ),
      @ApiResponse(
          responseCode = "404", description = "データベースに存在しないIDを指定した場合のレスポンスです。",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(
                      summary = "ID=999（データベースに存在しないID）を指定した場合",
                      value = """
                                {
                                   "status": "NOT_FOUND",
                                   "message": "レシピID「999」は存在しません",
                                   "errors": null
                                }
                          """
                  )
              }
          )
      )
  })
  @GetMapping("/{id}")
  public ResponseEntity<RecipeDetail> getRecipeDetail(@PathVariable int id) {
    RecipeDetail recipeDetail = recipeService.searchRecipeDetail(id);
    return ResponseEntity.ok(recipeDetail);
  }

  @Operation(
      summary = "レシピの新規作成",
      description = "入力した情報に基づきレシピを新規作成するとともに、そのレシピへのパスを作成します。"
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "レシピ詳細情報および画像データ（base64形式）です。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RecipeDetailWithImageData.class),
          examples = {
              @ExampleObject(
                  summary = "レシピを新規作成する場合",
                  value = """
                      {
                        "recipeDetail": {
                            "recipe": {
                                "name": "炒り卵",
                                "imagePath": "test3/path",
                                "recipeSource": "https://------3.com",
                                "servings": "3人分",
                                "remark": "備考欄3",
                                "favorite": false
                            },
                            "ingredients": [
                                {
                                    "name": "卵",
                                    "quantity": "3個",
                                    "arrange": false
                                },
                                {
                                    "name": "サラダ油",
                                    "quantity": "適量",
                                    "arrange": false
                                },
                                {
                                    "name": "マヨネーズ",
                                    "quantity": "大さじ2",
                                    "arrange": true
                                },
                                {
                                    "name": "砂糖",
                                    "quantity": "大さじ1/2",
                                    "arrange": false
                                }
                            ],
                            "instructions": [
                                {
                                    "stepNumber": 1,
                                    "content": "卵を溶いて調味料を混ぜ、卵液を作る",
                                    "arrange": false
                                },
                                {
                                    "stepNumber": 2,
                                    "content": "フライパンに油をたらし、火にかける",
                                    "arrange": false
                                },
                                {
                                    "stepNumber": 3,
                                    "content": "卵液をフライパンに入れて焼きながらかき混ぜて完成",
                                    "arrange": false
                                }
                            ]
                        },
                        "imageData": null
                      }
                      """
              )
          }
      )
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "処理が成功した場合のレスポンスです。",
          content = @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = RecipeDetail.class)),
              examples = {
                  @ExampleObject(
                      summary = "新規作成が成功した場合",
                      value = """
                          {
                          "recipe": {
                              "name": "炒り卵",
                              "imagePath": "test3/path",
                              "recipeSource": "https://------3.com",
                              "servings": "3人分",
                              "remark": "備考欄3",
                              "favorite": false
                          },
                          "ingredients": [
                              {
                                  "name": "卵",
                                  "quantity": "3個",
                                  "arrange": false
                              },
                              {
                                  "name": "サラダ油",
                                  "quantity": "適量",
                                  "arrange": false
                              },
                              {
                                  "name": "マヨネーズ",
                                  "quantity": "大さじ2",
                                  "arrange": true
                              },
                              {
                                  "name": "砂糖",
                                  "quantity": "大さじ1/2",
                                  "arrange": false
                              }
                          ],
                          "instructions": [
                              {
                                  "stepNumber": 1,
                                  "content": "卵を溶いて調味料を混ぜ、卵液を作る",
                                  "arrange": false
                              },
                              {
                                  "stepNumber": 2,
                                  "content": "フライパンに油をたらし、火にかける",
                                  "arrange": false
                              },
                              {
                                  "stepNumber": 3,
                                  "content": "卵液をフライパンに入れて焼きながらかき混ぜて完成",
                                  "arrange": false
                              }
                          ]
                          }
                          """
                  )
              }
          )
      ),
      @ApiResponse(responseCode = "400", description = "許可されていないリクエスト（▽必須項目の未入力、▽画像以外のデータのアップロード、▽5MBを超える画像データのアップロード）をリクエストした場合のレスポンス",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(
                      summary = "必須項目が未入力の場合",
                      value = """
                                {
                                   "status": "BAD_REQUEST",
                                   "message": "バリデーションエラーです。入力フォームを確認してください",
                                   "errors": [
                                     {
                                       "field": "recipe.name",
                                       "message": "空白は許可されていません"
                                     },
                                     {
                                       "field": "ingredients[0].name",
                                       "message": "空白は許可されていません"
                                     },
                                   ]
                                }
                          """
                  ),
                  @ExampleObject(
                      summary = "画像以外のファイルをアップロードした場合",
                      value = """
                                {
                                   "status": "BAD_REQUEST",
                                   "message": "画像ファイルのみアップロード可能です",
                                   "errors": null
                          """
                  ),
                  @ExampleObject(
                      summary = "5MBを超える画像データをアップロードした場合",
                      value = """
                                {
                                   "status": "BAD_REQUEST",
                                   "message": "画像ファイルのサイズが大きすぎます。5MB以下にしてください",
                                   "errors": null
                          """
                  )
              }
          )
      )
  })
  @PostMapping("/new")
  public ResponseEntity<RecipeDetail> createRecipeDetail
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

  @Operation(summary = "レシピの更新", description = "既存情報の変更や材料・調理手順の追加・削除を行います。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RecipeDetail.class))
      ),
      @ApiResponse(responseCode = "400", description = "許可されていないリクエスト（▽必須項目の未入力、▽画像以外のデータのアップロード、▽5MBを超える画像データのアップロード、▽パスとリクエストパラメータにおけるレシピIDの不一致）をリクエストした場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(responseCode = "404", description = "データベースに存在しないIDを指定した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      )
  })
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

  @Operation(summary = "レシピのお気に入りフラグの更新", description = "指定したIDに紐づくレシピのお気に入りフラグ（favorite）の更新を行います。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RecipeDetail.class))
      ),
      @ApiResponse(responseCode = "404", description = "データベースに存在しないIDを指定した場合のレスポンス",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PutMapping("/{id}/favorite")
  public ResponseEntity<String> updateFavoriteStatus(@PathVariable int id,
      @RequestBody Map<String, Boolean> request) {
    Boolean favorite = request.get("favorite");
    recipeService.updateFavoriteStatus(id, favorite);
    return ResponseEntity.ok("お気に入りを変更しました");
  }

  @Operation(summary = "レシピの削除", description = "指定したIDに紐づくレシピをデータベースから削除します。レシピIDに紐づく材料および調理手順もデータベースから削除します。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンス",
          content = @Content(mediaType = "text/plain",
              schema = @Schema(type = "string", example = "レシピを削除しました"))
      ),
      @ApiResponse(responseCode = "404", description = "データベースに存在しないIDを指定した場合のレスポンス",
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
