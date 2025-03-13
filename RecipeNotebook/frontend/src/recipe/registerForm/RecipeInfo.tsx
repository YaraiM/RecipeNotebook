import {
  FormControl,
  FormLabel,
  Input,
  Checkbox,
  VStack,
  Textarea,
} from "@chakra-ui/react";
import { RecipeState } from "../../types/RecipeState";

type Props = {
  recipe: RecipeState;
  onRecipeChange: <Key extends keyof RecipeState>(
    field: Key,
    value: RecipeState[Key],
  ) => void;
  onImageChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
};

export const RecipeInfo = ({
  recipe,
  onRecipeChange,
  onImageChange,
}: Props) => {
  return (
    <VStack spacing={4} align="stretch">
      <FormControl>
        <FormLabel>レシピ名（必須）</FormLabel>
        <Input
          value={recipe.name}
          onChange={(e) => onRecipeChange("name", e.target.value)}
        />
      </FormControl>

      <FormControl>
        <FormLabel>レシピ画像（ファイルサイズ：5MB以下までOK）</FormLabel>
        <Input
          type="file"
          accept="image/*"
          onChange={(e) => onImageChange(e)}
        />
      </FormControl>

      <FormControl>
        <FormLabel>レシピ情報元</FormLabel>
        <Input
          value={recipe.recipeSource}
          onChange={(e) => onRecipeChange("recipeSource", e.target.value)}
        />
      </FormControl>

      <FormControl>
        <FormLabel>何人分</FormLabel>
        <Input
          value={recipe.servings}
          onChange={(e) => onRecipeChange("servings", e.target.value)}
        />
      </FormControl>

      <FormControl>
        <FormLabel>備考</FormLabel>
        <Textarea
          value={recipe.remark}
          onChange={(e) => onRecipeChange("remark", e.target.value)}
          rows={3}
        />
      </FormControl>

      <Checkbox
        isChecked={recipe.favorite}
        onChange={(e) => onRecipeChange("favorite", e.target.checked)}
      >
        お気に入り
      </Checkbox>
    </VStack>
  );
};
