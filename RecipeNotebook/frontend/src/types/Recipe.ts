export type Recipe = {
  id?: number;
  userId?: number;
  name: string;
  imagePath?: string;
  recipeSource?: string;
  servings?: string;
  remark?: string;
  favorite: boolean;
  createdAt?: Date;
  updatedAt?: Date;
};
