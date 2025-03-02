import { Box, Button, Heading, HStack, VStack } from "@chakra-ui/react";
import { AddIcon } from "@chakra-ui/icons";
import { IngredientItem } from "./IngredientItem";
import { Ingredient } from "../types/Ingredient";

type Props = {
  ingredients: Ingredient[];
  setIngredients: React.Dispatch<React.SetStateAction<Ingredient[]>>;
};

export const IngredientList = ({ ingredients, setIngredients }: Props) => {
  const addIngredient = () => {
    setIngredients([
      ...ingredients,
      { name: "", quantity: "", arrange: false },
    ]);
  };

  const removeIngredient = (index: number) => {
    if (ingredients.length === 1) {
      return alert("材料は最低一つは必要です");
    }
    const newIngredients = [...ingredients];
    newIngredients.splice(index, 1);
    setIngredients(newIngredients);
  };

  const handleIngredientsChange = <Key extends keyof Ingredient>(
    index: number,
    field: Key,
    value: Ingredient[Key],
  ) => {
    const newIngredients = [...ingredients];
    newIngredients[index][field] = value;
    setIngredients(newIngredients);
  };

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
            onRemove={() => removeIngredient(index)}
          />
        ))}
      </VStack>
    </Box>
  );
};
