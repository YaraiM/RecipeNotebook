package raisetech.RecipeNotebook.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import raisetech.RecipeNotebook.exception.DateRangeValidation;

/**
 * レシピ詳細情報の一覧検索時に指定可能なパラメータをまとめたオブジェクトです。
 */
@Schema(description = "レシピ検索時に指定可能なパラメータ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DateRangeValidation
public class RecipeSearchCriteria {

  @Schema(description = "レシピ名で検索します。複数のキーワードを指定可能です。", example = "[\"目玉\", \"焼\"]")
  private List<String> recipeNames;

  @Schema(description = "お気に入りのレシピで絞り込みます。", example = "true")
  private Boolean favoriteRecipe;

  @Schema(description = "作成日（指定日以降）で検索します", example = "2024-01-01")
  private LocalDate createDateFrom;

  @Schema(description = "作成日（指定日以前）で検索します", example = "2025-01-01")
  private LocalDate createDateTo;

  @Schema(description = "更新日（指定日以降）で検索します", example = "2024-07-01")
  private LocalDate updateDateFrom;

  @Schema(description = "作成日（指定日以前）で検索します", example = "2025-07-01")
  private LocalDate updateDateTo;

  @Schema(description = "指定した材料が含まれるレシピを検索します。複数のキーワードを指定可能です。", example = "[\"卵\", \"水\"]")
  private List<String> ingredientNames;

}
