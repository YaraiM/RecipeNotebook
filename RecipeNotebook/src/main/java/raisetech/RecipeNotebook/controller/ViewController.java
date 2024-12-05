package raisetech.RecipeNotebook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

  @GetMapping("/login")
  public String loginForm() {
    return "login";
  }

  @GetMapping("/recipes")
  public String recipeList() {
    return "recipes";
  }

  @GetMapping("/recipes/{id}/detail")
  public String recipeDetail(@PathVariable int id) {
    return "detail";
  }

  @GetMapping("/recipes/new")
  public String registerForm() {
    return "new";
  }

  @GetMapping("/recipes/{id}/update")
  public String updateForm(@PathVariable int id) {
    return "update";
  }

}
