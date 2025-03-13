import { VStack, Divider } from "@chakra-ui/react";
import { NavigationBar } from "../layout/NavigationBar";
import { useRecipe } from "../hooks/use-recipe";
import { SearchForm } from "../recipe/recipeList/SearchForm";
import { Buttons } from "../recipe/recipeList/Buttons";
import { RecipeCards } from "../recipe/recipeList/RecipeCards";

export const Recipes = () => {
  const {
    recipeDetails,
    recipeFilterText,
    ingredientFilterText,
    filterFavorite,
    dateFilter,
    handleRecipeFilter,
    handleIngredientFilter,
    handleFilterFavorite,
    handleDateFilter,
    removeFilter,
    loadRecipeDetails,
    toggleFavorite,
    deleteRecipe,
  } = useRecipe();

  return (
    <>
      <NavigationBar />
      <VStack spacing={3} align="stretch">
        <SearchForm
          recipeFilterText={recipeFilterText}
          ingredientFilterText={ingredientFilterText}
          filterFavorite={filterFavorite}
          dateFilter={dateFilter}
          handleRecipeFilter={handleRecipeFilter}
          handleIngredientFilter={handleIngredientFilter}
          handleFilterFavorite={handleFilterFavorite}
          handleDateFilter={handleDateFilter}
          removeFilter={removeFilter}
        />

        <Divider />

        <Buttons
          loadRecipeDetails={loadRecipeDetails}
          removeFilter={removeFilter}
        />

        <Divider />

        <RecipeCards
          recipeDetails={recipeDetails}
          toggleFavorite={toggleFavorite}
          deleteRecipe={deleteRecipe}
        />
      </VStack>
    </>
  );
};
