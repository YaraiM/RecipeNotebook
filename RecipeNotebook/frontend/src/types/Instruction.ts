export type Instruction = {
  id?: number;
  recipeId?: number;
  stepNumber: number;
  content: string;
  arrange: boolean;
};
