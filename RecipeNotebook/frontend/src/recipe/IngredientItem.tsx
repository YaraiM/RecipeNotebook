import {
  Checkbox,
  FormControl,
  HStack,
  IconButton,
  Input,
} from "@chakra-ui/react";
import { Ingredient } from "../types/Ingredient";
import { DeleteIcon } from "@chakra-ui/icons";

type Props = {
  ingredient: Ingredient;
  index: number;
  onChange: <Key extends keyof Ingredient>(
    index: number,
    field: Key,
    value: Ingredient[Key],
  ) => void;
  onRemove: (index: number) => void;
};

export const IngredientItem = ({
  ingredient,
  index,
  onChange,
  onRemove,
}: Props) => {
  return (
    <HStack spacing={3} align="center">
      <FormControl flex="5">
        <Input
          placeholder="材料名（必須）"
          value={ingredient.name}
          onChange={(e) => onChange(index, "name", e.target.value)}
        />
      </FormControl>

      <FormControl flex="3">
        <Input
          placeholder="分量"
          value={ingredient.quantity}
          onChange={(e) => onChange(index, "quantity", e.target.value)}
        />
      </FormControl>

      <Checkbox
        isChecked={ingredient.arrange}
        onChange={(e) => onChange(index, "arrange", e.target.checked)}
      >
        アレンジ
      </Checkbox>

      <IconButton
        icon={<DeleteIcon />}
        colorScheme="red"
        aria-label="材料を削除"
        onClick={() => onRemove(index)}
        size="sm"
      />
    </HStack>
  );
};
