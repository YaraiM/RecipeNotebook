package raisetech.RecipeNotebook.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import raisetech.RecipeNotebook.domain.RecipeDetailWithImageData;

public class RecipeApiRequests {

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = """
          レシピ詳細情報および画像データ（base64形式）です。

          ・レシピ、材料、調理手順のいずれもid、recipeIdの入力は不要です。

          ・画像ファイルを入力しなかった場合は「no_image.jpg」のイメージパスを返します。
          """,
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RecipeDetailWithImageData.class),
          examples = {
              @ExampleObject(
                  name = "createRecipeRequest",
                  summary = "レシピを新規作成する場合",
                  description = "recipe, ingredients, instructionsを入力しています。imageDataはnullとしています。",
                  value = """
                      {
                        "recipeDetail": {
                            "recipe": {
                                "name": "炒り卵",
                                "imagePath": "test3/path",
                                "recipeSource": "https://------3.com",
                                "servings": "3人分",
                                "remark": "基本の炒り卵のレシピです。",
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
  public @interface CreateRecipeRequest {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = """
          レシピ詳細情報および画像データ（base64形式）です。

          ・レシピ情報にはidが必要です。

          ・材料および調理手順情報のrecipeIdは入力不要です。

          ・材料および調理手順情報のidを入力した場合は、既存データの変更になります。

          ・材料および調理手順情報のidを入力しなかった場合は、新規データの追加になります。

          ・既存の材料または調理手順が入力されていない場合、そのデータは削除されます。

          ・画像ファイルを入力した場合は既存画像ファイルを削除して新規画像ファイルを登録します。
          """,
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RecipeDetailWithImageData.class),
          examples = {
              @ExampleObject(
                  name = "updateRecipeRequest",
                  summary = "レシピ名・備考欄の変更、砂糖の削除、白だしの追加をする場合",
                  description = """
                      ・recipeのrecipeNameとremarkをそれぞれ変更しています。

                      ・ingredientsのid=4で登録されていた「砂糖」の情報をリクエストに記載していません。

                      ・新規材料として、ingredientsに白だしをid=nullで記載しています。
                      """,
                  value = """
                      {
                        "recipeDetail": {
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
                              "name": "卵",
                              "quantity": "3個",
                              "arrange": false
                            },
                            {
                              "id": 2,
                              "name": "サラダ油",
                              "quantity": "適量",
                              "arrange": false
                            },
                            {
                              "id": 3,
                              "name": "醤油",
                              "quantity": "大さじ1/2",
                              "arrange": false
                            },
                            {
                              "id": null,
                              "name": "白だし",
                              "quantity": "小さじ1/2",
                              "arrange": true
                            }
                          ],
                          "instructions": [
                            {
                              "id": 1,
                              "stepNumber": 1,
                              "content": "卵を溶いて調味料を混ぜ、卵液を作る",
                              "arrange": false
                            },
                            {
                              "id": 2,
                              "stepNumber": 2,
                              "content": "フライパンに油をたらし、火にかける",
                              "arrange": false
                            },
                            {
                              "id": 3,
                              "stepNumber": 3,
                              "content": "卵液を1/3くらいフライパンに入れて焼き、巻く",
                              "arrange": true
                            },
                            {
                              "id": 4,
                              "stepNumber": 4,
                              "content": "3の手順を繰り返して完成",
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
  public @interface UpdateRecipeRequest {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "お気に入り状態を変更したいレシピのIDと変更後のお気に入り状態を入力します。",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = RecipeDetailWithImageData.class),
          examples = {
              @ExampleObject(
                  name = "updateFavoriteStatus",
                  summary = "ID=1のレシピ（卵焼き）をお気に入りにする場合",
                  description = "レシピのID=1のお気に入り状態をtrueに変更するリクエストです。",
                  value = """
                          {
                            "id": 1,
                            "favorite": true
                          }
                      """
              )
          }
      )
  )
  public @interface UpdateFavoriteStatusRequest {

  }

}
