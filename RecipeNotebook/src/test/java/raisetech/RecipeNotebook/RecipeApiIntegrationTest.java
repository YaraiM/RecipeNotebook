package raisetech.RecipeNotebook;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import raisetech.RecipeNotebook.data.Ingredient;
import raisetech.RecipeNotebook.data.Instruction;
import raisetech.RecipeNotebook.data.Recipe;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
import raisetech.RecipeNotebook.repository.RecipeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RecipeApiIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RecipeRepository repository;

  @ParameterizedTest
  @MethodSource("provideSearchExistingRecipeTestCase")
  void レシピの一覧検索_リクエストパラメータに応じたレシピ詳細情報が存在する場合にその一覧を取得できること(
      RecipeSearchCriteria inputCriteria, List<RecipeDetail> expectedRecipeDetails)
      throws Exception {
    mockMvc.perform(get("/recipes")
            .param("recipeNames", getParamStringListOrNull(inputCriteria.getRecipeNames()))
            .param("favoriteRecipe", getParamBooleanOrNull(inputCriteria.getFavoriteRecipe()))
            .param("createDateFrom", getParamLocalDateOrNull(inputCriteria.getCreateDateFrom()))
            .param("createDateTo", getParamLocalDateOrNull(inputCriteria.getCreateDateTo()))
            .param("updateDateFrom", getParamLocalDateOrNull(inputCriteria.getUpdateDateFrom()))
            .param("updateDateTo", getParamLocalDateOrNull(inputCriteria.getUpdateDateTo()))
            .param("ingredientNames", getParamStringListOrNull(inputCriteria.getIngredientNames()))
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(expectedRecipeDetails.size()))
        .andExpect(jsonPath("$[*].recipe.id",
            contains(extractRecipeField(expectedRecipeDetails, Recipe::getId).toArray())))
        .andExpect(jsonPath("$[*].recipe.name",
            contains(extractRecipeField(expectedRecipeDetails, Recipe::getName).toArray())))
        .andExpect(jsonPath("$[*].recipe.imagePath",
            contains(extractRecipeField(expectedRecipeDetails, Recipe::getImagePath).toArray())))
        .andExpect(jsonPath("$[*].recipe.recipeSource",
            contains(extractRecipeField(expectedRecipeDetails, Recipe::getRecipeSource).toArray())))
        .andExpect(jsonPath("$[*].recipe.servings",
            contains(extractRecipeField(expectedRecipeDetails, Recipe::getServings).toArray())))
        .andExpect(jsonPath("$[*].recipe.remark",
            contains(extractRecipeField(expectedRecipeDetails, Recipe::getRemark).toArray())))
        .andExpect(jsonPath("$[*].recipe.favorite",
            contains(extractRecipeField(expectedRecipeDetails, Recipe::isFavorite).toArray())))
        .andExpect(jsonPath("$[*].recipe.createdAt").exists())
        .andExpect(jsonPath("$[*].recipe.updatedAt").exists())
        .andExpect(jsonPath("$[*].ingredients[*].id",
            contains(extractIngredientField(expectedRecipeDetails, Ingredient::getId).toArray())))
        .andExpect(jsonPath("$[*].ingredients[*].recipeId",
            contains(
                extractIngredientField(expectedRecipeDetails, Ingredient::getRecipeId).toArray())))
        .andExpect(jsonPath("$[*].ingredients[*].name",
            contains(extractIngredientField(expectedRecipeDetails, Ingredient::getName).toArray())))
        .andExpect(jsonPath("$[*].ingredients[*].quantity",
            contains(
                extractIngredientField(expectedRecipeDetails, Ingredient::getQuantity).toArray())))
        .andExpect(jsonPath("$[*].ingredients[*].arrange",
            contains(
                extractIngredientField(expectedRecipeDetails, Ingredient::isArrange).toArray())))
        .andExpect(jsonPath("$[*].instructions[*].id",
            contains(extractInstructionField(expectedRecipeDetails, Instruction::getId).toArray())))
        .andExpect(jsonPath("$[*].instructions[*].recipeId",
            contains(extractInstructionField(expectedRecipeDetails,
                Instruction::getRecipeId).toArray())))
        .andExpect(jsonPath("$[*].instructions[*].stepNumber",
            contains(extractInstructionField(expectedRecipeDetails,
                Instruction::getStepNumber).toArray())))
        .andExpect(jsonPath("$[*].instructions[*].content",
            contains(
                extractInstructionField(expectedRecipeDetails, Instruction::getContent).toArray())))
        .andExpect(jsonPath("$[*].instructions[*].arrange",
            contains(
                extractInstructionField(expectedRecipeDetails, Instruction::isArrange).toArray())));
  }

  /**
   * レシピ詳細情報一覧検索のテストケースです。ここでは、結果が返ってくるもののみを実施しています。
   * @return テストケース（検索結果1件以上）
   */
  private static Stream<Arguments> provideSearchExistingRecipeTestCase() {
    return Stream.of(
        // 条件指定なし（全件検索）
        Arguments.of(
            new RecipeSearchCriteria(null, null, null, null, null, null, null),
            java.util.List.of(createExpectedRecipeDetail1(), createExpectedRecipeDetail2())),
        // レシピ１のみ合致する条件をすべてのcriteriaに入力
        Arguments.of(
            new RecipeSearchCriteria(List.of("卵", "焼"), false, LocalDate.of(2024, 9, 21),
                LocalDate.of(2024, 9, 23), LocalDate.of(2024, 10, 21),
                LocalDate.of(2024, 10, 23), List.of("卵", "糖")),
            List.of(createExpectedRecipeDetail1())),
        // 空文字列が条件指定された場合は無視される
        Arguments.of(
            new RecipeSearchCriteria(List.of("卵", ""), null, null, null, null, null,
                List.of("", "砂糖")),
            List.of(createExpectedRecipeDetail1()))
    );
  }

  @ParameterizedTest
  @MethodSource("provideSearchNotExistingRecipeTestCase")
  void レシピの一覧検索_リクエストパラメータに合致するデータがない場合に検索結果が0件であること(
      RecipeSearchCriteria inputCriteria)
      throws Exception {
    mockMvc.perform(get("/recipes")
            .param("recipeNames", getParamStringListOrNull(inputCriteria.getRecipeNames()))
            .param("favoriteRecipe", getParamBooleanOrNull(inputCriteria.getFavoriteRecipe()))
            .param("createDateFrom", getParamLocalDateOrNull(inputCriteria.getCreateDateFrom()))
            .param("createDateTo", getParamLocalDateOrNull(inputCriteria.getCreateDateTo()))
            .param("updateDateFrom", getParamLocalDateOrNull(inputCriteria.getUpdateDateFrom()))
            .param("updateDateTo", getParamLocalDateOrNull(inputCriteria.getUpdateDateTo()))
            .param("ingredientNames", getParamStringListOrNull(inputCriteria.getIngredientNames()))
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0))
        .andExpect(jsonPath("$").isEmpty());
  }

  /**
   * レシピ詳細情報一覧検索のテストケースです。ここでは、結果が返って来ない場合をテストしています。
   *　検索条件のバリデーションはControllerの単体テストで確認済みのため、省略しています。
   * @return テストケース（検索結果0件）
   */
  private static Stream<Arguments> provideSearchNotExistingRecipeTestCase() {
    return Stream.of(
        // レシピ名に存在しない材料がある
        Arguments.of(
            new RecipeSearchCriteria(List.of("存在しない材料", "卵"), null, null, null, null, null,
                null)),
        // 作成日が日付の指定範囲外にある
        Arguments.of(
            new RecipeSearchCriteria(null, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                null, null, null)),
        // 更新日が日付の指定範囲外にある
        Arguments.of(
            new RecipeSearchCriteria(null, null, null, null, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), null)),
        // 材料名に存在しない材料がある
        Arguments.of(
            new RecipeSearchCriteria(null, null, null, null, null, null,
                List.of("存在しない材料", "卵")))
    );
  }

  @Test
  void レシピの検索_正常系_存在するIDを指定した場合にレシピ詳細情報が返ってくること()
      throws Exception {
    mockMvc.perform(get("/recipes/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.recipe.id").value(createExpectedRecipeDetail1().getRecipe().getId()))
        .andExpect(
            jsonPath("$.recipe.name").value(createExpectedRecipeDetail1().getRecipe().getName()))
        .andExpect(jsonPath("$.recipe.imagePath").value(
            createExpectedRecipeDetail1().getRecipe().getImagePath()))
        .andExpect(jsonPath("$.recipe.recipeSource").value(
            createExpectedRecipeDetail1().getRecipe().getRecipeSource()))
        .andExpect(jsonPath("$.recipe.servings").value(
            createExpectedRecipeDetail1().getRecipe().getServings()))
        .andExpect(jsonPath("$.recipe.remark").value(
            createExpectedRecipeDetail1().getRecipe().getRemark()))
        .andExpect(jsonPath("$.recipe.favorite").value(
            createExpectedRecipeDetail1().getRecipe().isFavorite()))
        .andExpect(jsonPath("$.recipe.createdAt").exists())
        .andExpect(jsonPath("$.recipe.updatedAt").exists())
        .andExpect(jsonPath("$.ingredients[*].id",
            contains(createExpectedRecipeDetail1().getIngredients().stream().map(Ingredient::getId)
                .toArray())))
        .andExpect(jsonPath("$.ingredients[*].recipeId",
            contains(
                createExpectedRecipeDetail1().getIngredients().stream().map(Ingredient::getRecipeId)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].name",
            contains(
                createExpectedRecipeDetail1().getIngredients().stream().map(Ingredient::getName)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].quantity",
            contains(
                createExpectedRecipeDetail1().getIngredients().stream().map(Ingredient::getQuantity)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].arrange",
            contains(
                createExpectedRecipeDetail1().getIngredients().stream().map(Ingredient::isArrange)
                    .toArray())))
        .andExpect(jsonPath("$.instructions[*].id",
            contains(
                createExpectedRecipeDetail1().getInstructions().stream().map(Instruction::getId)
                    .toArray())))
        .andExpect(jsonPath("$.instructions[*].recipeId",
            contains(createExpectedRecipeDetail1().getInstructions().stream()
                .map(Instruction::getRecipeId).toArray())))
        .andExpect(jsonPath("$.instructions[*].stepNumber",
            contains(createExpectedRecipeDetail1().getInstructions().stream()
                .map(Instruction::getStepNumber).toArray())))
        .andExpect(jsonPath("$.instructions[*].content",
            contains(createExpectedRecipeDetail1().getInstructions().stream()
                .map(Instruction::getContent).toArray())))
        .andExpect(jsonPath("$.instructions[*].arrange",
            contains(
                createExpectedRecipeDetail1().getInstructions().stream().map(Instruction::isArrange)
                    .toArray())));
  }

  @Test
  void レシピの検索_異常系_存在しないレシピIDを指定したときに例外がスローされること()
      throws Exception {
    mockMvc.perform(get("/recipes/{id}", 999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("レシピID「" + 999 + "」は存在しません"));
  }

  @Test
  void レシピの新規作成_JSON形式のリクエストボディを指定して新規作成できること()
      throws Exception {
    mockMvc.perform(post("/recipes/new")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
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
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location",
            containsString("/recipes/" + createExpectedRecipeDetail3().getRecipe().getId())))
        .andExpect(
            jsonPath("$.recipe.id").value(createExpectedRecipeDetail3().getRecipe().getId()))
        .andExpect(
            jsonPath("$.recipe.name").value(createExpectedRecipeDetail3().getRecipe().getName()))
        .andExpect(jsonPath("$.recipe.imagePath").value(
            createExpectedRecipeDetail3().getRecipe().getImagePath()))
        .andExpect(jsonPath("$.recipe.recipeSource").value(
            createExpectedRecipeDetail3().getRecipe().getRecipeSource()))
        .andExpect(jsonPath("$.recipe.servings").value(
            createExpectedRecipeDetail3().getRecipe().getServings()))
        .andExpect(jsonPath("$.recipe.remark").value(
            createExpectedRecipeDetail3().getRecipe().getRemark()))
        .andExpect(jsonPath("$.recipe.favorite").value(
            createExpectedRecipeDetail3().getRecipe().isFavorite()))
        .andExpect(jsonPath("$.recipe.createdAt").exists())
        .andExpect(jsonPath("$.recipe.updatedAt").doesNotExist())
        .andExpect(jsonPath("$.ingredients[*].id").exists()) // 更新メソッドで材料が一部削除された場合、IDがどうなるかわからないため
        .andExpect(jsonPath("$.ingredients[*].recipeId",
            contains(
                createExpectedRecipeDetail3().getIngredients().stream().map(Ingredient::getRecipeId)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].name",
            contains(
                createExpectedRecipeDetail3().getIngredients().stream().map(Ingredient::getName)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].quantity",
            contains(
                createExpectedRecipeDetail3().getIngredients().stream().map(Ingredient::getQuantity)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].arrange",
            contains(
                createExpectedRecipeDetail3().getIngredients().stream().map(Ingredient::isArrange)
                    .toArray())))
        .andExpect(jsonPath("$.instructions[*].id").exists())
        .andExpect(jsonPath("$.instructions[*].recipeId",
            contains(createExpectedRecipeDetail3().getInstructions().stream()
                .map(Instruction::getRecipeId).toArray())))
        .andExpect(jsonPath("$.instructions[*].stepNumber",
            contains(createExpectedRecipeDetail3().getInstructions().stream()
                .map(Instruction::getStepNumber).toArray())))
        .andExpect(jsonPath("$.instructions[*].content",
            contains(createExpectedRecipeDetail3().getInstructions().stream()
                .map(Instruction::getContent).toArray())))
        .andExpect(jsonPath("$.instructions[*].arrange",
            contains(
                createExpectedRecipeDetail3().getInstructions().stream().map(Instruction::isArrange)
                    .toArray())));
  }

  @Test
  void レシピの更新_正常系_JSON形式のリクエストボディを指定して更新できること()
      throws Exception {
    mockMvc.perform(put("/recipes/{id}/update", createExpectedRecipeDetail1().getRecipe().getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "recipe": {
                        "id": 1,
                        "name": "卵焼きrev",
                        "imagePath": "test1/path/rev",
                        "recipeSource": "https://------1/rev.com",
                        "servings": "2人分rev",
                        "remark": "備考欄1rev",
                        "favorite": true
                    },
                    "ingredients": [
                        {
                            "id": 1,
                            "name": "卵rev",
                            "quantity": "3個rev",
                            "arrange": true
                        },
                        {
                            "id": 2,
                            "name": "サラダ油rev",
                            "quantity": "適量rev",
                            "arrange": true
                        },
                        {
                            "id": 3,
                            "name": "醤油rev",
                            "quantity": "大さじ1/2rev",
                            "arrange": true
                        },
                        {
                            "name": "胡椒",
                            "quantity": "適量",
                            "arrange": true
                        }
                    ],
                    "instructions": [
                        {
                            "id": 1,
                            "stepNumber": 1,
                            "content": "卵を溶いて調味料を混ぜ、卵液を作るrev",
                            "arrange": true
                        },
                        {
                            "id": 2,
                            "stepNumber": 2,
                            "content": "フライパンに油をたらし、火にかけるrev",
                            "arrange": true
                        },
                        {
                            "id": 3,
                            "stepNumber": 3,
                            "content": "卵液を1/3くらいフライパンに入れて焼き、巻くrev",
                            "arrange": true
                        },
                        {
                            "stepNumber": 4,
                            "content": "胡椒をかけて完成",
                            "arrange": true
                        }
                    ]
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.recipe.id").value(createUpdatedRecipeDetail1().getRecipe().getId()))
        .andExpect(
            jsonPath("$.recipe.name").value(createUpdatedRecipeDetail1().getRecipe().getName()))
        .andExpect(jsonPath("$.recipe.imagePath").value(
            createUpdatedRecipeDetail1().getRecipe().getImagePath()))
        .andExpect(jsonPath("$.recipe.recipeSource").value(
            createUpdatedRecipeDetail1().getRecipe().getRecipeSource()))
        .andExpect(jsonPath("$.recipe.servings").value(
            createUpdatedRecipeDetail1().getRecipe().getServings()))
        .andExpect(jsonPath("$.recipe.remark").value(
            createUpdatedRecipeDetail1().getRecipe().getRemark()))
        .andExpect(jsonPath("$.recipe.favorite").value(
            createUpdatedRecipeDetail1().getRecipe().isFavorite()))
        .andExpect(result -> {
              String responseContent = result.getResponse().getContentAsString();
              JsonNode jsonNode = objectMapper.readTree(responseContent);
              String actualUpdatedAtStr = jsonNode.path("recipe").path("updatedAt")
                  .asText();
              LocalDateTime actualUpdatedAt = LocalDateTime.parse(actualUpdatedAtStr);
              assertTrue(actualUpdatedAt.isAfter(
                  createExpectedRecipeDetail1().getRecipe().getUpdatedAt()));
            }
        )
        .andExpect(jsonPath("$.ingredients[*].id").exists())
        .andExpect(jsonPath("$.ingredients[*].recipeId",
            contains(
                createUpdatedRecipeDetail1().getIngredients().stream().map(Ingredient::getRecipeId)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].name",
            contains(
                createUpdatedRecipeDetail1().getIngredients().stream().map(Ingredient::getName)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].quantity",
            contains(
                createUpdatedRecipeDetail1().getIngredients().stream().map(Ingredient::getQuantity)
                    .toArray())))
        .andExpect(jsonPath("$.ingredients[*].arrange",
            contains(
                createUpdatedRecipeDetail1().getIngredients().stream().map(Ingredient::isArrange)
                    .toArray())))
        .andExpect(jsonPath("$.instructions[*].id").exists())
        .andExpect(jsonPath("$.instructions[*].recipeId",
            contains(createUpdatedRecipeDetail1().getInstructions().stream()
                .map(Instruction::getRecipeId).toArray())))
        .andExpect(jsonPath("$.instructions[*].stepNumber",
            contains(createUpdatedRecipeDetail1().getInstructions().stream()
                .map(Instruction::getStepNumber).toArray())))
        .andExpect(jsonPath("$.instructions[*].content",
            contains(createUpdatedRecipeDetail1().getInstructions().stream()
                .map(Instruction::getContent).toArray())))
        .andExpect(jsonPath("$.instructions[*].arrange",
            contains(
                createUpdatedRecipeDetail1().getInstructions().stream().map(Instruction::isArrange)
                    .toArray())));
  }

  @Test
  void レシピの更新_異常系_パスのIDとリクエストボディのレシピIDが異なる場合に例外をスローすること()
      throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/recipes/{id}/update", 999)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                        "recipe": {
                            "id": 1,
                            "name": "卵焼きrev",
                            "imagePath": "test1/path/rev",
                            "recipeSource": "https://------1/rev.com",
                            "servings": "2人分rev",
                            "remark": "備考欄1rev",
                            "favorite": true
                        },
                        "ingredients": [
                            {
                                "id": 1,
                                "name": "卵rev",
                                "quantity": "3個rev",
                                "arrange": true
                            }
                        ],
                        "instructions": [
                            {
                                "id": 1,
                                "stepNumber": 1,
                                "content": "卵を溶いて調味料を混ぜ、卵液を作るrev",
                                "arrange": true
                            }
                        ]
                    }
                    """
            ))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(
            "パスで指定したID「999」と更新対象のレシピのID「1」は一致させてください"));
  }

  @Test
  void レシピの更新_異常系_存在しないレシピIDを指定したときに例外をスローすること()
      throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/recipes/{id}/update", 999)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                        "recipe": {
                            "id": 999,
                            "name": "卵焼きrev",
                            "imagePath": "test1/path/rev",
                            "recipeSource": "https://------1/rev.com",
                            "servings": "2人分rev",
                            "remark": "備考欄1rev",
                            "favorite": true
                        },
                        "ingredients": [
                            {
                                "id": 1,
                                "name": "卵rev",
                                "quantity": "3個rev",
                                "arrange": true
                            }
                        ],
                        "instructions": [
                            {
                                "id": 1,
                                "stepNumber": 1,
                                "content": "卵を溶いて調味料を混ぜ、卵液を作るrev",
                                "arrange": true
                            }
                        ]
                    }
                    """
            ))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("レシピID「999」は存在しません"));
  }

  @Test
  void レシピの更新_異常系_存在しない材料IDを指定したときに例外をスローすること()
      throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/recipes/{id}/update",
                createExpectedRecipeDetail1().getRecipe().getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                        "recipe": {
                            "id": 1,
                            "name": "卵焼きrev",
                            "imagePath": "test1/path/rev",
                            "recipeSource": "https://------1/rev.com",
                            "servings": "2人分rev",
                            "remark": "備考欄1rev",
                            "favorite": true
                        },
                        "ingredients": [
                            {
                                "id": 999,
                                "name": "卵rev",
                                "quantity": "3個rev",
                                "arrange": true
                            }
                        ],
                        "instructions": [
                            {
                                "id": 1,
                                "stepNumber": 1,
                                "content": "卵を溶いて調味料を混ぜ、卵液を作るrev",
                                "arrange": true
                            }
                        ]
                    }
                    """
            ))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("材料ID「999」は存在しません"));
  }

  @Test
  void レシピの更新_異常系_存在しない調理手順IDを指定したときに例外をスローすること()
      throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/recipes/{id}/update",
                createExpectedRecipeDetail1().getRecipe().getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                        "recipe": {
                            "id": 1,
                            "name": "卵焼きrev",
                            "imagePath": "test1/path/rev",
                            "recipeSource": "https://------1/rev.com",
                            "servings": "2人分rev",
                            "remark": "備考欄1rev",
                            "favorite": true
                        },
                        "ingredients": [
                            {
                                "id": 1,
                                "name": "卵rev",
                                "quantity": "3個rev",
                                "arrange": true
                            }
                        ],
                        "instructions": [
                            {
                                "id": 999,
                                "stepNumber": 1,
                                "content": "卵を溶いて調味料を混ぜ、卵液を作るrev",
                                "arrange": true
                            }
                        ]
                    }
                    """
            ))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("調理手順ID「999」は存在しません"));
  }

  //  TODO：お気に入りフラグの切り替え機能をテスト

  @Test
  void レシピの削除_正常系_存在するIDを指定した場合にレシピおよび紐づく材料と調理手順が削除されメッセージが返ってくること()
      throws Exception {
    int recipeId = createExpectedRecipeDetail1().getRecipe().getId();

    mockMvc.perform(delete("/recipes/{id}/delete", recipeId))
        .andExpect(status().isOk())
        .andExpect(content().string("レシピを削除しました"));

    assertNull(repository.getRecipe(recipeId));
    assertThat(repository.getIngredients(recipeId), hasSize(0));
    assertThat(repository.getInstructions(recipeId), hasSize(0));
  }

  @Test
  void レシピの削除_異常系_存在しないレシピIDを指定したときに例外がスローされること()
      throws Exception {
    mockMvc.perform(delete("/recipes/{id}/delete", 999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("レシピID「" + 999 + "」は存在しません"));
  }


  private String getParamStringListOrNull(List<String> parameter) {
    return parameter != null ? String.join(",", parameter) : null;
  }

  private String getParamBooleanOrNull(Boolean parameter) {
    return parameter != null ? String.valueOf(parameter) : null;
  }

  private String getParamLocalDateOrNull(LocalDate parameter) {
    return parameter != null ? String.valueOf(parameter) : null;
  }

  private <T> List<T> extractRecipeField(List<RecipeDetail> details,
      Function<Recipe, T> extractor) {
    return details.stream().map(d -> extractor.apply(d.getRecipe())).collect(Collectors.toList());
  }

  private <T> List<T> extractIngredientField(List<RecipeDetail> details,
      Function<Ingredient, T> extractor) {
    return details.stream()
        .flatMap(detail -> detail.getIngredients().stream())
        .map(extractor)
        .collect(Collectors.toList());
  }

  private <T> List<T> extractInstructionField(List<RecipeDetail> details,
      Function<Instruction, T> extractor) {
    return details.stream()
        .flatMap(detail -> detail.getInstructions().stream())
        .map(extractor)
        .collect(Collectors.toList());
  }

  private static RecipeDetail createExpectedRecipeDetail1() {
    return new RecipeDetail(
        new Recipe(1, "卵焼き", "test1/path", "https://------1.com", "2人分", "備考欄1",
            false, LocalDateTime.of(2024, 9, 22, 17, 0, 0),
            LocalDateTime.of(2024, 10, 22, 17, 0, 0)),
        List.of(
            new Ingredient(1, 1, "卵", "3個", false),
            new Ingredient(2, 1, "サラダ油", "適量", false),
            new Ingredient(3, 1, "醤油", "大さじ1/2", false),
            new Ingredient(4, 1, "砂糖", "大さじ1", false)
        ),
        List.of(
            new Instruction(1, 1, 1, "卵を溶いて調味料を混ぜ、卵液を作る", false),
            new Instruction(2, 1, 2, "フライパンに油をたらし、火にかける", false),
            new Instruction(3, 1, 3, "卵液を1/3くらいフライパンに入れて焼き、巻く",
                true),
            new Instruction(4, 1, 4, "3の手順を繰り返して完成", false)
        )
    );
  }

  private static RecipeDetail createExpectedRecipeDetail2() {
    return new RecipeDetail(
        new Recipe(2, "目玉焼き", "test2/path", "https://------2.com", "1人分",
            "備考欄2", true, LocalDateTime.of(2024, 9, 23, 17, 0, 0),
            LocalDateTime.of(2024, 10, 23, 17, 0, 0)),
        List.of(
            new Ingredient(5, 2, "卵", "1個", false),
            new Ingredient(6, 2, "サラダ油", "適量", false),
            new Ingredient(7, 2, "水", null, false)
        ),
        List.of(
            new Instruction(5, 2, 1, "フライパンに油をたらし、火にかける", false),
            new Instruction(6, 2, 2, "フライパンに卵を割り入れる", false),
            new Instruction(7, 2, 3,
                "少し焼けたら水を入れ、ふたをして5分、弱火にかけて完成", false)
        )
    );
  }

  private static RecipeDetail createExpectedRecipeDetail3() {
    return new RecipeDetail(
        new Recipe(3, "炒り卵", "test3/path", "https://------3.com", "3人分",
            "備考欄3", false, null, null),
        List.of(
            new Ingredient(3, "卵", "3個", false),
            new Ingredient(3, "サラダ油", "適量", false),
            new Ingredient(3, "マヨネーズ", "大さじ2", true),
            new Ingredient(3, "砂糖", "大さじ1/2", false)
        ),
        List.of(
            new Instruction(3, 1, "卵を溶いて調味料を混ぜ、卵液を作る", false),
            new Instruction(3, 2, "フライパンに油をたらし、火にかける", false),
            new Instruction(3, 3,
                "卵液をフライパンに入れて焼きながらかき混ぜて完成", false)
        )
    );
  }

  private static RecipeDetail createUpdatedRecipeDetail1() {
    return new RecipeDetail(
        new Recipe(1, "卵焼きrev", "test1/path/rev", "https://------1/rev.com", "2人分rev",
            "備考欄1rev",
            true, LocalDateTime.of(2024, 9, 22, 17, 0, 0),
            LocalDateTime.of(2024, 11, 22, 17, 0, 0)),
        List.of(
            new Ingredient(1, 1, "卵rev", "3個rev", true),
            new Ingredient(2, 1, "サラダ油rev", "適量rev", true),
            new Ingredient(3, 1, "醤油rev", "大さじ1/2rev", true),
            // id=4の材料は削除されている
            new Ingredient(1, "胡椒", "適量", true) //新規追加
        ),
        List.of(
            new Instruction(1, 1, 1, "卵を溶いて調味料を混ぜ、卵液を作るrev", true),
            new Instruction(2, 1, 2, "フライパンに油をたらし、火にかけるrev", true),
            new Instruction(3, 1, 3, "卵液を1/3くらいフライパンに入れて焼き、巻くrev",
                true),
            // id=4の調理手順は削除されている
            new Instruction(1, 4, "胡椒をかけて完成", true) //新規追加の調理手順
        )
    );
  }

}