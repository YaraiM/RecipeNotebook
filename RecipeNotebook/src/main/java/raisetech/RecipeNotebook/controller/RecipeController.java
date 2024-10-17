package raisetech.RecipeNotebook.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import raisetech.RecipeNotebook.domain.RecipeDetail;
import raisetech.RecipeNotebook.domain.RecipeSearchCriteria;
import raisetech.RecipeNotebook.service.RecipeService;

@Controller
@Validated
public class RecipeController {

  private final RecipeService service;

  @Autowired
  public RecipeController(RecipeService service) {
    this.service = service;
  }

  @GetMapping("/recipes")
  public String getRecipes(RecipeSearchCriteria criteria, Model model) {
    List<RecipeDetail> recipeDetails = service.searchRecipeList(criteria);
    model.addAttribute("recipeSummaries", recipeDetails);
    return "recipes";
  }

  @GetMapping("/recipes/{id}")
  public String getRecipeDetail(@PathVariable int id, Model model) {
    RecipeDetail recipeDetail = service.searchRecipeDetail(id);
    model.addAttribute("recipeDetail", recipeDetail);
    return "recipeDetail";
  }

  @GetMapping("/recipes/new")
  public String newRecipeDetail(Model model) {
    model.addAttribute("recipeDetailInput", new RecipeDetail());
    return "registerRecipeDetail";
  }

  //  TODO：▲バリデーションのメッセージを調整する（参考書11.17）⇒例外ハンドラは不要？メッセージプロパティで変更できない？
  @PostMapping("/recipes/new/validate-input")
  public String validateRecipeDetail(
      @Valid @ModelAttribute("recipeDetailInput") RecipeDetail recipeDetailInput,
      BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      return "registerRecipeDetail";
    }
    service.registerRecipeDetail(recipeDetailInput);
    return "registerRecipeDetailConfirm";//TODO:★★入力内容を出力する画面へ遷移　⇒　「確定」または「変更」をできるようにする（参考書11-8）
  }

  @GetMapping("/recipes/new/confirm")

}

//TODO:ログイン画面を作成（参考書14章）