package raisetech.RecipeNotebook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RecipeViewController {

  @GetMapping("/recipes")
  public String showRecipeList() {
    return "recipes";
  }

  @GetMapping("/recipes/{id}/detail")
  public String showRecipeDetail(@PathVariable int id) {
    return "detail";
  }

  @GetMapping("/recipes/new")
  public String showNewRecipeForm() {
    return "new";
  }

  // レシピ更新画面
  @GetMapping("/recipes/{id}/update")
  public String showUpdateForm(@PathVariable int id) {
    return "update";
  }

}
