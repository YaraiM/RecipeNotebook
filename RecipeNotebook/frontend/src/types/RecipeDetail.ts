import { Ingredient } from "./Ingredient";
import { Instruction } from "./Instruction";
import { Recipe } from "./Recipe";

export type RecipeDetail = {
  recipe: Recipe;
  ingredients: Ingredient[];
  instructions: Instruction[];
};
