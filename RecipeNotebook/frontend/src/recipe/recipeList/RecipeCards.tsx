import {
  SimpleGrid,
  Card,
  CardHeader,
  HStack,
  Box,
  CardBody,
  VStack,
  CardFooter,
  Text,
  Image,
} from "@chakra-ui/react";
import { RecipeDetail } from "../../types/RecipeDetail";
import { useNavigate } from "react-router-dom";

type Props = {
  recipeDetails: RecipeDetail[];
  toggleFavorite: (recipeDetail: RecipeDetail) => Promise<void>;
  deleteRecipe: (recipeDetail: RecipeDetail) => Promise<void>;
};

export const RecipeCards = ({
  recipeDetails,
  toggleFavorite,
  deleteRecipe,
}: Props) => {
  const navigate = useNavigate();

  return (
    <>
      <SimpleGrid spacing={4} columns={{ base: 1, md: 2, lg: 3, xl: 4 }}>
        {recipeDetails.map((recipeDetail) => (
          <Card
            key={recipeDetail.recipe.id}
            width="100%"
            maxWidth="300px"
            height="auto"
            position="relative"
            onClick={() =>
              navigate(`/recipes/${recipeDetail.recipe.id}`, {
                replace: true,
              })
            }
            cursor="pointer"
          >
            <CardHeader py={1}>
              <HStack>
                <Box
                  fontSize="2xl"
                  cursor="pointer"
                  onClick={(e) => {
                    toggleFavorite(recipeDetail);
                    e.stopPropagation();
                  }}
                >
                  {recipeDetail.recipe.favorite ? "★" : "☆"}
                </Box>
                <Box
                  fontSize="2xl"
                  cursor="pointer"
                  onClick={(e) => {
                    navigate(`/recipes/${recipeDetail.recipe.id}/update`, {
                      replace: true,
                    });
                    e.stopPropagation();
                  }}
                >
                  ✎
                </Box>
                <Box
                  fontSize="3xl"
                  cursor="pointer"
                  onClick={(e) => {
                    deleteRecipe(recipeDetail);
                    e.stopPropagation();
                  }}
                >
                  ×
                </Box>
              </HStack>
            </CardHeader>
            <CardBody py={1} display="flex" justifyContent="center">
              <VStack spacing={1}>
                <Image
                  src={`http://localhost:8080${recipeDetail.recipe.imagePath}`}
                  alt={recipeDetail.recipe.name}
                  width="100%"
                  height="auto"
                  maxHeight="150px"
                  objectFit="contain"
                />
                <Text fontSize="md" textAlign="left">
                  {recipeDetail.recipe.name}
                </Text>
              </VStack>
            </CardBody>
            <CardFooter pt={0}>
              <VStack spacing={0} alignItems="flex-start" width="100%">
                <Text fontSize="xs">
                  作成日：
                  {recipeDetail.recipe.createdAt
                    ? new Intl.DateTimeFormat("ja-JP").format(
                        new Date(recipeDetail.recipe.createdAt),
                      )
                    : ""}
                </Text>
                <Text fontSize="xs">
                  更新日：
                  {recipeDetail.recipe.updatedAt
                    ? new Intl.DateTimeFormat("ja-JP").format(
                        new Date(recipeDetail.recipe.updatedAt),
                      )
                    : ""}
                </Text>
              </VStack>
            </CardFooter>
          </Card>
        ))}
      </SimpleGrid>
    </>
  );
};
