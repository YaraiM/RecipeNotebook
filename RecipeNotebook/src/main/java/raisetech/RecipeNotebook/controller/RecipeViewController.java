package raisetech.RecipeNotebook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecipeViewController {

  @GetMapping("/recipes")
  public String showRecipeList() {
    return "recipes";
  }

  @GetMapping("/recipes/detail")
  public String showRecipeDetail() {
    return "detail";
  }

  @GetMapping("/recipes/new")
  public String showNewRecipeForm() {
    return "new";
  }

  // レシピ更新画面
  @GetMapping("/recipes/update")
  public String showUpdateForm() {
    return "update";
  }

}
