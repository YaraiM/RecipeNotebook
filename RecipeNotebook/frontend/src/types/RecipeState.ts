export type RecipeState = {
  name: string;
  recipeSource: string;
  servings: string;
  remark: string;
  favorite: boolean;
  image: File | null;
  imageSelected: boolean;
};
