package raisetech.RecipeNotebook.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.exception.ErrorResponse;

public class RecipeApiResponses {

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "一覧検索の処理が成功した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RecipeDetail.class),
          examples = {
              @ExampleObject(
                  name = "success(find 1 recipe)",
                  summary = "検索条件に合致するレシピが1件見つかった場合",
                  description = "見つかったレシピの一覧を返します。",
                  value = """
                      [
                          {
                              "recipe": {
                                  "id": 2,
                                  "userId": 1,
                                  "name": "目玉焼き",
                                  "imagePath": "/images/medamayaki.jpg",
                                  "recipeSource": null,
                                  "servings": "1人前",
                                  "remark": "基本的な目玉焼きのレシピ。",
                                  "favorite": false,
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
  )
  public @interface SearchRecipesSuccess {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "400",
      description = "検索条件で不正な日付を指定した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
              @ExampleObject(
                  name = "illegalDateRange",
                  summary = "終了日が開始日より前の日付を指定した場合",
                  description = "不正な日付が入力されたフィールドとエラーメッセージを返します。",
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
  public @interface SearchRecipesBadRequest {

  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @SearchRecipesSuccess
  @SearchRecipesBadRequest
  public @interface SearchRecipesResponses {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200", description = "指定したIDのレシピ詳細情報の取得が成功した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RecipeDetail.class),
          examples = {
              @ExampleObject(
                  name = "success(find a recipe with ID：2)",
                  summary = "存在するID：2を指定した場合",
                  description = "見つかったレシピ（ID=2）を返します。",
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
  )
  public @interface GetRecipeDetailSuccess {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "404",
      description = "データベースに存在しないレシピIDを指定した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
              @ExampleObject(
                  name = "recipeIdNotFound",
                  summary = "存在しないレシピID：999を指定した場合",
                  description = "エラーステータスとメッセージを返します。",
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
  public @interface RecipeIdNotFound {

  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @GetRecipeDetailSuccess
  @RecipeIdNotFound
  public @interface GetRecipeDetailResponses {

  }


  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "201", description = "レシピの新規作成処理が成功した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RecipeDetail.class),
          examples = {
              @ExampleObject(
                  name = "success(create a recipe)",
                  summary = "新規作成が成功した場合",
                  description = "作成したレシピの詳細情報を返します。",
                  value = """
                      {
                      	"recipe": {
                      		"id": 3,
                      		"name": "炒り卵",
                      		"imagePath": "test3/path",
                      		"recipeSource": "https://------3.com",
                      		"servings": "3人分",
                      		"remark": "基本の炒り卵のレシピです。",
                      		"favorite": false
                      	},
                      	"ingredients": [
                      		{
                      			"id": 8,
                      			"recipeId": 3,
                      			"name": "卵",
                      			"quantity": "3個",
                      			"arrange": false
                      		},
                      		{
                      			"id": 9,
                      			"recipeId": 3,
                      			"name": "サラダ油",
                      			"quantity": "適量",
                      			"arrange": false
                      		},
                      		{
                      			"id": 10,
                      			"recipeId": 3,
                      			"name": "マヨネーズ",
                      			"quantity": "大さじ2",
                      			"arrange": true
                      		},
                      		{
                      			"id": 11,
                      			"recipeId": 3,
                      			"name": "砂糖",
                      			"quantity": "大さじ1/2",
                      			"arrange": false
                      		}
                      	],
                      	"instructions": [
                      		{
                      			"id": 8,
                      			"recipeId": 3,
                      			"stepNumber": 1,
                      			"content": "卵を溶いて調味料を混ぜ、卵液を作る",
                      			"arrange": false
                      		},
                      		{
                      			"id": 9,
                      			"recipeId": 3,
                      			"stepNumber": 2,
                      			"content": "フライパンに油をたらし、火にかける",
                      			"arrange": false
                      		},
                      		{
                      			"id": 10,
                      			"recipeId": 3,
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
  )
  public @interface CreateRecipeSuccess {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "400",
      description = "許可されていないリクエストをした場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
              @ExampleObject(
                  name = "notEmptyField",
                  summary = "必須項目が未入力の場合",
                  description = "空白のフィールドとエラーメッセージを返します。",
                  value = """
                      {
                          "status": "BAD_REQUEST",
                          "message": "バリデーションエラーです。入力フォームを確認してください",
                          "errors": [
                              {
                                  "field": "recipeDetail.ingredients[0].name",
                                  "message": "空白は許可されていません"
                              },
                              {
                                  "field": "recipeDetail.recipe.name",
                                  "message": "空白は許可されていません"
                              }
                          ]
                      }
                      """
              ),
              @ExampleObject(
                  name = "invalidFileType",
                  summary = "画像以外のファイルを送信した場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                          "status": "BAD_REQUEST",
                          "message": "画像ファイルのみアップロード可能です",
                          "errors": null
                      }
                      """
              ),
              @ExampleObject(
                  name = "fileSizeLimitExceeded",
                  summary = "5MBを超える画像を選択してリクエストした場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                          "status": "BAD_REQUEST",
                          "message": "画像ファイルのサイズが大きすぎます。5MB以下にしてください",
                          "errors": null
                      }
                      """
              )
          }
      )
  )
  public @interface CreateFormValidation {

  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @CreateRecipeSuccess
  @CreateFormValidation
  public @interface CreateRecipeResponses {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(responseCode = "200", description = "更新処理が成功した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RecipeDetail.class),
          examples = {
              @ExampleObject(
                  name = "success(update a recipe)",
                  summary = "更新が成功した場合",
                  description = "更新されたレシピの詳細情報を返します。",
                  value = """
                      {
                        "recipe": {
                          "id": 1,
                          "name": "卵焼き（アレンジver.）",
                          "imagePath": "/images/tamagoyaki.jpg",
                          "recipeSource": "https://------1.com",
                          "servings": "2人分",
                          "remark": "アレンジ版の卵焼きのレシピです。砂糖の代わりに白だしを加えます",
                          "favorite": false
                        },
                        "ingredients": [
                          {
                            "id": 1,
                            "recipeId": 1,
                            "name": "卵",
                            "quantity": "3個",
                            "arrange": false
                          },
                          {
                            "id": 2,
                            "recipeId": 1,
                            "name": "サラダ油",
                            "quantity": "適量",
                            "arrange": false
                          },
                          {
                            "id": 3,
                            "recipeId": 1,
                            "name": "醤油",
                            "quantity": "大さじ1/2",
                            "arrange": false
                          },
                          {
                            "id": 5,
                            "recipeId": 1,
                            "name": "白だし",
                            "quantity": "小さじ1/2",
                            "arrange": true
                          }
                        ],
                        "instructions": [
                          {
                            "id": 1,
                            "recipeId": 1,
                            "stepNumber": 1,
                            "content": "卵を溶いて調味料を混ぜ、卵液を作る",
                            "arrange": false
                          },
                          {
                            "id": 2,
                            "recipeId": 1,
                            "stepNumber": 2,
                            "content": "フライパンに油をたらし、火にかける",
                            "arrange": false
                          },
                          {
                            "id": 3,
                            "recipeId": 1,
                            "stepNumber": 3,
                            "content": "卵液を1/3くらいフライパンに入れて焼き、巻く",
                            "arrange": true
                          },
                          {
                            "id": 4,
                            "recipeId": 1,
                            "stepNumber": 4,
                            "content": "3の手順を繰り返して完成",
                            "arrange": false
                          }
                        ]
                      }
                      """
              )
          }
      )
  )
  public @interface UpdateRecipeSuccess {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "400",
      description = "許可されていないリクエストをした場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
              @ExampleObject(
                  name = "notEmptyField",
                  summary = "必須項目が未入力の場合",
                  description = "空白のフィールドとエラーメッセージを返します。",
                  value = """
                      {
                          "status": "BAD_REQUEST",
                          "message": "バリデーションエラーです。入力フォームを確認してください",
                          "errors": [
                              {
                                  "field": "recipeDetail.ingredients[0].name",
                                  "message": "空白は許可されていません"
                              },
                              {
                                  "field": "recipeDetail.recipe.name",
                                  "message": "空白は許可されていません"
                              }
                          ]
                      }
                      """
              ),
              @ExampleObject(
                  name = "invalidFileType",
                  summary = "画像以外のファイルを送信した場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                          "status": "BAD_REQUEST",
                          "message": "画像ファイルのみアップロード可能です",
                          "errors": null
                      }
                      """
              ),
              @ExampleObject(
                  name = "fileSizeLimitExceeded",
                  summary = "5MBを超える画像を選択してリクエストした場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                          "status": "BAD_REQUEST",
                          "message": "画像ファイルのサイズが大きすぎます。5MB以下にしてください",
                          "errors": null
                      }
                      """
              ),
              @ExampleObject(
                  name = "recipeIdMismatch",
                  summary = "パスとリクエストパラメータのIDが不一致の場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                          "status": "BAD_REQUEST",
                          "message": "パスで指定したID「{id}」と更新対象のレシピのID「{id}」は一致させてください",
                          "errors": null
                      }
                      """
              )
          }
      )
  )
  public @interface UpdateFormValidation {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "404",
      description = "データベースに存在しないIDを指定した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
              @ExampleObject(
                  name = "recipeIdNotFound",
                  summary = "存在しないレシピID：999を指定した場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                         "status": "NOT_FOUND",
                         "message": "レシピID「999」は存在しません",
                         "errors": null
                      }
                      """
              ),
              @ExampleObject(
                  name = "ingredientIdNotFound",
                  summary = "存在しない材料ID：999を指定した場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                         "status": "NOT_FOUND",
                         "message": "材料ID「999」は存在しません",
                         "errors": null
                      }
                      """
              ),
              @ExampleObject(
                  name = "instructionIdNotFound",
                  summary = "存在しない調理手順ID：999を指定した場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                         "status": "NOT_FOUND",
                         "message": "調理手順ID「999」は存在しません",
                         "errors": null
                      }
                      """
              )
          }
      )
  )
  public @interface IdNotFound {

  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @UpdateRecipeSuccess
  @UpdateFormValidation
  @IdNotFound
  public @interface UpdateRecipeResponses {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "お気に入りフラグの変更処理が成功した場合のレスポンスです。",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(type = "string"),
          examples = {
              @ExampleObject(
                  name = "success(update a favorite status)",
                  summary = "お気に入り状態を変更した場合",
                  description = "メッセージを返します。",
                  value = "お気に入りを変更しました"
              )
          }
      )
  )
  public @interface UpdateFavoriteStatusSuccess {

  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @UpdateFavoriteStatusSuccess
  @RecipeIdNotFound
  public @interface UpdateFavoriteStatusResponses {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "レシピの削除処理が成功した場合のレスポンスです。",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(type = "string"),
          examples = {
              @ExampleObject(
                  name = "success(delete a recipe)",
                  summary = "メッセージの削除が成功した場合",
                  description = "メッセージを返します。",
                  value = "レシピを削除しました"
              )
          }
      )
  )
  public @interface DeleteRecipeSuccess {

  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @DeleteRecipeSuccess
  @RecipeIdNotFound
  public @interface DeleteRecipeResponses {

  }

}
