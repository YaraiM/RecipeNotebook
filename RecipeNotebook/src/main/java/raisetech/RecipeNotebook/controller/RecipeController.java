package raisetech.RecipeNotebook.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
  public String getRecipes(Model model, @Valid @ModelAttribute
  RecipeSearchCriteria criteria) {
    List<RecipeDetail> recipeDetails = service.searchRecipeList(criteria);
    model.addAttribute("recipeSummaries", recipeDetails);
    return "recipes";
  }

  @GetMapping("/recipes/{id}")
  public String getRecipeDetail(Model model, @PathVariable int id) {
    RecipeDetail recipeDetail = service.searchRecipeDetail(id);
    model.addAttribute("recipeDetail", recipeDetail);
    return "recipeDetail";
  }

}
