import { Box, Divider, Heading, VStack } from "@chakra-ui/react";
import { RecipeInfo } from "../recipe/registerForm/RecipeInfo";
import { IngredientList } from "../recipe/registerForm/IngredientList";
import { FormActions } from "../recipe/registerForm/FormActions";
import { InstructionList } from "../recipe/registerForm/InstructionList";
import { NavigationBar } from "../layout/NavigationBar";
import { useRecipe } from "../hooks/use-recipe";

export const RecipeForm = () => {
  const {
    recipe,
    ingredients,
    setIngredients,
    instructions,
    setInstructions,
    formTitle,
    handleRecipeChange,
    handleImageChange,
    addIngredient,
    removeIngredient,
    handleIngredientsChange,
    addIntruction,
    removeInstruction,
    handleInstructionsChange,
    handleFormSubmit,
  } = useRecipe();

  return (
    <>
      <NavigationBar />
      <Box as="form" onSubmit={handleFormSubmit}>
        <VStack spacing={6} align="stretch">
          <Heading as="h1" size="xl" textAlign="center" mb={4}>
            {formTitle}
          </Heading>

          <RecipeInfo
            recipe={recipe}
            onRecipeChange={handleRecipeChange}
            onImageChange={handleImageChange}
          />

          <Divider />

          <IngredientList
            ingredients={ingredients}
            setIngredients={setIngredients}
            addIngredient={addIngredient}
            removeIngredient={removeIngredient}
            handleIngredientsChange={handleIngredientsChange}
          />

          <Divider />

          <InstructionList
            instructions={instructions}
            setInstructions={setInstructions}
            addIntruction={addIntruction}
            removeInstruction={removeInstruction}
            handleInstructionsChange={handleInstructionsChange}
          />

          <FormActions />
        </VStack>
      </Box>
    </>
  );
};
