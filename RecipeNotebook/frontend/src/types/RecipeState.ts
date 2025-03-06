export type RecipeState = {
  id?: number;
  userId?: number;
  name: string;
  recipeSource: string;
  servings: string;
  remark: string;
  favorite: boolean;
  image?: File;
  imageSelected?: boolean;
};
