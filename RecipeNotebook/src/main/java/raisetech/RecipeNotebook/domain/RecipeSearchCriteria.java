package raisetech.RecipeNotebook.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO:必要な検索条件を追加し、テストを行う
@Schema(description = "レシピ詳細情報検索時に指定可能なパラメータ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchCriteria {

  @Schema(description = "レシピの名前（部分一致）")
  private String recipeName;

  @Schema(description = "レシピに使用している材料名（部分一致）")
  private String ingredientName;

}
