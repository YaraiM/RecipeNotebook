package raisetech.RecipeNotebook.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import raisetech.RecipeNotebook.config.SecurityConfig;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
import raisetech.RecipeNotebook.exception.RecipeIdMismatchException;
import raisetech.RecipeNotebook.service.RecipeService;

@WebMvcTest(RecipeApiController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "user", roles = "USER")
class RecipeApiControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  RecipeService recipeService;

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeAll
  public static void setUp() {
    Locale.setDefault(new Locale("ja", "JP"));
  }

  @Test
  void レシピの一覧検索_サービスの処理が適切に呼び出されて処理成功のレスポンスが返ってくること()
      throws Exception {
    mockMvc.perform(get("/api/recipes"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService, times(1)).searchRecipeList(any(RecipeSearchCriteria.class));
  }

  @Test
  void レシピ詳細情報検索_サービスの処理が適切に呼び出されて処理成功のレスポンスが返ってくること()
      throws Exception {
    int recipeId = 1;
    RecipeDetail mockDetail = createTestRecipeDetail(recipeId);
    when(recipeService.searchRecipeDetail(recipeId)).thenReturn(mockDetail);

    mockMvc.perform(get("/api/recipes/{id}", recipeId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService, times(1)).searchRecipeDetail(recipeId);
  }

  @Test
  void レシピの新規登録_エンドポイントでサービスの処理が適切に呼び出され新規作成のレスポンスとURIが返ってくること()
      throws Exception {
    int newRecipeId = 2;
    RecipeDetail mockRecipeDetail = createTestRecipeDetail(newRecipeId);

    // JSONからサービスメソッドに渡される引数をキャプチャ
    ArgumentCaptor<RecipeDetail> recipeDetailCaptor = ArgumentCaptor.forClass(RecipeDetail.class);
    ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);

    when(recipeService.createRecipeDetail(recipeDetailCaptor.capture(), fileCaptor.capture()))
        .thenReturn(mockRecipeDetail);

    mockMvc.perform(post("/api/recipes/new")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
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
            ))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location",
            "http://localhost/recipes/" + mockRecipeDetail.getRecipe().getId()))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService, times(1)).createRecipeDetail(recipeDetailCaptor.capture(),
        fileCaptor.capture());
  }

  @Test
  void レシピ詳細情報の更新_正常系_エンドポイントでサービスの処理が適切に呼び出され処理成功のレスポンスが返ってくること()
      throws Exception {
    int existingRecipeId = 3;
    RecipeDetail mockRecipeDetail = createTestRecipeDetail(existingRecipeId);

    // JSONからサービスメソッドに渡される引数をキャプチャ
    ArgumentCaptor<RecipeDetail> recipeDetailCaptor = ArgumentCaptor.forClass(RecipeDetail.class);
    ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);

    when(recipeService.updateRecipeDetail(recipeDetailCaptor.capture(),
        fileCaptor.capture())).thenReturn(
        mockRecipeDetail);

    mockMvc.perform(put("/api/recipes/{id}/update", existingRecipeId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                        {
                            "recipeDetail": {
                                "recipe": {
                                    "id": 3,
                                    "name": "炒り卵",
                                    "imagePath": "test3/path",
                                    "recipeSource": "https://------3.com",
                                    "servings": "3人分",
                                    "remark": "備考欄3",
                                    "favorite": false
                                },
                                "ingredients": [
                                    {
                                        "id": 8,
                                        "name": "卵",
                                        "quantity": "3個",
                                        "arrange": false
                                    },
                                    {
                                        "id": 9,
                                        "name": "サラダ油",
                                        "quantity": "適量",
                                        "arrange": false
                                    },
                                    {
                                        "id": 10,
                                        "name": "マヨネーズ",
                                        "quantity": "大さじ2",
                                        "arrange": true
                                    },
                                    {
                                        "id": 11,
                                        "name": "砂糖",
                                        "quantity": "大さじ1/2",
                                        "arrange": false
                                    }
                                ],
                                "instructions": [
                                    {
                                        "id": 8,
                                        "stepNumber": 1,
                                        "content": "卵を溶いて調味料を混ぜ、卵液を作る",
                                        "arrange": false
                                    },
                                    {
                                        "id": 9,
                                        "stepNumber": 2,
                                        "content": "フライパンに油をたらし、火にかける",
                                        "arrange": false
                                    },
                                    {
                                        "id": 10,
                                        "stepNumber": 3,
                                        "content": "卵液をフライパンに入れて焼きながらかき混ぜて完成",
                                        "arrange": false
                                    },
                                    {
                                        "stepNumber": 4,
                                        "content": "黒コショウをかけて味変",
                                        "arrange": true
                                    }
                                ]
                            },
                            "imageData": null
                        }
                    """
            ))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService, times(1)).updateRecipeDetail(recipeDetailCaptor.capture(),
        fileCaptor.capture());
  }

  @Test
  void レシピ詳細情報の更新_異常系_パスのIDとリクエストボディのレシピIDが一致していない場合に例外をスローすること()
      throws Exception {
    int pathId = 1;
    int inputRecipeId = 2;

    String expectedErrorMessage =
        "パスで指定したID「" + pathId + "」と更新対象のレシピのID「" + inputRecipeId
        + "」は一致させてください";

    doThrow(new RecipeIdMismatchException(expectedErrorMessage))
        .when(recipeService).updateRecipeDetail(any(RecipeDetail.class), any(MultipartFile.class));

    mockMvc.perform(put("/api/recipes/{id}/update", pathId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                       {
                           "recipeDetail": {
                               "recipe": {
                                   "id": 2,
                                   "name": "炒り卵",
                                   "imagePath": "test3/path",
                                   "recipeSource": "https://------3.com",
                                   "servings": "3人分",
                                   "remark": "備考欄3",
                                   "favorite": false
                               },
                               "ingredients": [
                                   {
                                       "id": 8,
                                       "name": "卵",
                                       "quantity": "3個",
                                       "arrange": false
                                   },
                                   {
                                       "id": 9,
                                       "name": "サラダ油",
                                       "quantity": "適量",
                                       "arrange": false
                                   },
                                   {
                                       "id": 10,
                                       "name": "マヨネーズ",
                                       "quantity": "大さじ2",
                                       "arrange": true
                                   },
                                   {
                                       "id": 11,
                                       "name": "砂糖",
                                       "quantity": "大さじ1/2",
                                       "arrange": false
                                   }
                               ],
                               "instructions": [
                                   {
                                       "id": 8,
                                       "stepNumber": 1,
                                       "content": "卵を溶いて調味料を混ぜ、卵液を作る",
                                       "arrange": false
                                   },
                                   {
                                       "id": 9,
                                       "stepNumber": 2,
                                       "content": "フライパンに油をたらし、火にかける",
                                       "arrange": false
                                   },
                                   {
                                       "id": 10,
                                       "stepNumber": 3,
                                       "content": "卵液をフライパンに入れて焼きながらかき混ぜて完成",
                                       "arrange": false
                                   },
                                   {
                                       "stepNumber": 4,
                                       "content": "黒コショウをかけて味変",
                                       "arrange": true
                                   }
                               ]
                           },
                           "imageData": null
                       }
                    """
            ))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(expectedErrorMessage));

  }

  @Test
  void お気に入りフラグの切替_エンドポイントでIDに紐づくレシピのお気に入り切替処理が呼び出され処理成功レスポンスとメッセージが返ること()
      throws Exception {
    int recipeId = 1;
    boolean favorite = false;

    mockMvc.perform(put("/api/recipes/{id}/favorite", recipeId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"favorite\": false}")
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/plain;charset=UTF-8"))
        .andExpect(content().string("お気に入りを変更しました"));

    verify(recipeService, times(1)).updateFavoriteStatus(recipeId, favorite);
  }

  @Test
  void レシピの削除_エンドポイントでサービスの処理が適切に呼び出され処理成功のレスポンスとメッセージが返ってくること()
      throws Exception {
    int recipeId = 1;

    mockMvc.perform(delete("/api/recipes/{id}/delete", recipeId).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/plain;charset=UTF-8"))
        .andExpect(content().string("レシピを削除しました"));

    verify(recipeService, times(1)).deleteRecipe(recipeId);
  }

  @Test
  void 条件検索の入力チェック_リクエストパラメータがすべて適切な場合に入力チェックがかからないこと()
      throws Exception {
    RecipeSearchCriteria criteria = new RecipeSearchCriteria(); // criteriaをすべて指定しないケース

    Set<ConstraintViolation<RecipeSearchCriteria>> violations = validator.validate(criteria);

    assertThat(violations, hasSize(0));
  }

  @Test
  void 条件検索の入力チェック_リクエストパラメータのうち入力値のルールがある項目が不適切な場合に入力チェックがかかること()
      throws Exception {
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();

    // 日付範囲の指定で開始日が終了日より後の日付になっている
    criteria.setCreateDateFrom(LocalDate.parse("2024-09-23"));
    criteria.setCreateDateTo(LocalDate.parse("2024-09-22"));
    criteria.setUpdateDateFrom(LocalDate.parse("2024-10-23"));
    criteria.setUpdateDateTo(LocalDate.parse("2024-10-22"));

    Set<ConstraintViolation<RecipeSearchCriteria>> violations = validator.validate(criteria);

    assertThat(violations, hasSize(2));
  }

  @Test
  void 新規登録および更新の入力チェック_リクエストボディがすべて適切な場合に入力チェックがかからないこと()
      throws Exception {
    int recipeId = 1;
    RecipeDetail recipeDetail = createTestRecipeDetail(recipeId);

    Set<ConstraintViolation<RecipeDetail>> violations = validator.validate(recipeDetail);

    assertThat(violations, hasSize(0));
  }

  @Test
  void 新規登録および更新の入力チェック_リクエストボディのうち入力値のルールがある項目が不適切な場合に入力チェックがかかること()
      throws Exception {
    int recipeId = 111;
    RecipeDetail recipeDetail = createTestRecipeDetail(recipeId);

    recipeDetail.getRecipe().setName(""); // nameは空白を許容しない
    recipeDetail.getIngredients().get(0).setName(""); // nameは空白を許容しない
    recipeDetail.getInstructions().get(0).setContent("");// contentは空白を許容しない

    Set<ConstraintViolation<RecipeDetail>> violations = validator.validate(recipeDetail);

    assertThat(violations, hasSize(3));
  }

  /**
   * テスト用にRecipeDetailオブジェクトを作成するメソッドです。
   *
   * @return レシピ詳細情報
   */
  private static RecipeDetail createTestRecipeDetail(int recipeId) {
    Recipe recipe = new Recipe(recipeId, 1, "testName", "testImage", "testSource", "testServings",
        "testRemark", false,
        LocalDateTime.parse("2000-01-01T00:00:00"), LocalDateTime.parse("2001-01-01T00:00:00"));

    Ingredient ingredient1 = new Ingredient(1, recipeId, "testName1", "testQuantity1", false);
    Ingredient ingredient2 = new Ingredient(2, recipeId, "testName2", "testQuantity2", true);
    List<Ingredient> ingredients = new ArrayList<>(List.of(ingredient1, ingredient2));

    Instruction instruction1 = new Instruction(1, recipeId, 1, "testContent1", false);
    Instruction instruction2 = new Instruction(2, recipeId, 2, "testContent2", true);
    List<Instruction> instructions = new ArrayList<>(List.of(instruction1, instruction2));

    return new RecipeDetail(recipe, ingredients, instructions);
  }

}
