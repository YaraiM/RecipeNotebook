import { Box, Button, Heading, HStack, VStack } from "@chakra-ui/react";
import { AddIcon } from "@chakra-ui/icons";
import { IngredientItem } from "./IngredientItem";
import { Ingredient } from "../../types/Ingredient";

type Props = {
  ingredients: Ingredient[];
  setIngredients: React.Dispatch<React.SetStateAction<Ingredient[]>>;
  addIngredient: () => void;
  removeIngredient: (index: number) => void;
  handleIngredientsChange: <Key extends keyof Ingredient>(
    index: number,
    field: Key,
    value: Ingredient[Key],
  ) => void;
};

export const IngredientList = ({
  ingredients,
  addIngredient,
  removeIngredient,
  handleIngredientsChange,
}: Props) => {
  return (
    <Box>
      <HStack justify="space-between" mb={4}>
        <Heading as="h5" size="md">
          材料
        </Heading>
        <Button
          size="sm"
          leftIcon={<AddIcon />}
          colorScheme="blue"
          variant="outline"
          onClick={addIngredient}
        >
          材料を追加
        </Button>
      </HStack>

      <VStack spacing={3} align="stretch">
        {ingredients.map((ingredient, index) => (
          <IngredientItem
            key={index}
            ingredient={ingredient}
            index={index}
            onChange={handleIngredientsChange}
            onRemove={removeIngredient}
          />
        ))}
      </VStack>
    </Box>
  );
};
