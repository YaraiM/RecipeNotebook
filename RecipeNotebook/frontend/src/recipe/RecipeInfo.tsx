import {
  FormControl,
  FormLabel,
  Input,
  Checkbox,
  VStack,
  Heading,
  Textarea,
} from "@chakra-ui/react";
import { RecipeState } from "../types/RecipeState";

type Props = {
  recipe: RecipeState;
  onChange: <Key extends keyof RecipeState>(
    field: Key,
    value: RecipeState[Key],
  ) => void;
};

export const RecipeInfo = ({ recipe, onChange }: Props) => {
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      onChange("image", file);
      onChange("imageSelected", true);
    }
  };

  return (
    <VStack spacing={4} align="stretch">
      <Heading as="h1" size="xl" textAlign="center" mb={4}>
        レシピ新規作成フォーム
      </Heading>

      <FormControl>
        <FormLabel>レシピ名（必須）</FormLabel>
        <Input
          value={recipe.name}
          onChange={(e) => onChange("name", e.target.value)}
        />
      </FormControl>

      <FormControl>
        <FormLabel>レシピ画像（ファイルサイズ：5MB以下までOK）</FormLabel>
        <Input type="file" accept="image/*" onChange={handleImageChange} />
      </FormControl>

      <FormControl>
        <FormLabel>レシピ情報元</FormLabel>
        <Input
          value={recipe.recipeSource}
          onChange={(e) => onChange("recipeSource", e.target.value)}
        />
      </FormControl>

      <FormControl>
        <FormLabel>何人分</FormLabel>
        <Input
          value={recipe.servings}
          onChange={(e) => onChange("servings", e.target.value)}
        />
      </FormControl>

      <FormControl>
        <FormLabel>備考</FormLabel>
        <Textarea
          value={recipe.remark}
          onChange={(e) => onChange("remark", e.target.value)}
          rows={3}
        />
      </FormControl>

      <Checkbox
        isChecked={recipe.favorite}
        onChange={(e) => onChange("favorite", e.target.checked)}
      >
        お気に入り
      </Checkbox>
    </VStack>
  );
};
