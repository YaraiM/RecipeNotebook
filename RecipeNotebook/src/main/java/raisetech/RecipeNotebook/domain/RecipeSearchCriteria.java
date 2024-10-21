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
@Schema(description = "レシピ詳細情報の一覧検索時に指定可能なパラメータ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DateRangeValidation
public class RecipeSearchCriteria {

  @Schema(description = "レシピの名前によるキーワード検索（部分一致）")
  private List<String> recipeNames;

  @Schema(description = "レシピのお気に入りフラグ")
  private Boolean favoriteRecipe;

  @Schema(description = "レシピの作成日の範囲検索（起点）")
  private LocalDate createDateFrom;

  @Schema(description = "レシピの作成日の範囲検索（終点）")
  private LocalDate createDateTo;

  @Schema(description = "レシピの更新日の範囲検索（起点）")
  private LocalDate updateDateFrom;

  @Schema(description = "レシピの更新日の範囲検索（終点）")
  private LocalDate updateDateTo;

  @Schema(description = "レシピに使用している材料名（部分一致）")
  private List<String> ingredientNames;

}
