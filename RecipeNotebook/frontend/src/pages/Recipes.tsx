import {
  VStack,
  Text,
  Divider,
  HStack,
  FormControl,
  Input,
  Checkbox,
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  SimpleGrid,
  Image,
  Box,
  Flex,
} from "@chakra-ui/react";
import { useNavigate } from "react-router-dom";
import { NavigationBar } from "../layout/NavigationBar";
import { useRecipe } from "../hooks/use-recipe";

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

  const navigate = useNavigate();

  return (
    <>
      <NavigationBar />
      <VStack spacing={3} align="stretch">
        <Flex
          direction={{ base: "column", md: "row" }}
          gap={2}
          align="flex-end"
        >
          <FormControl flex={{ base: "1", md: "2" }} maxW={{ md: "250px" }}>
            <Input
              value={recipeFilterText}
              onChange={(e) => handleRecipeFilter(e.target.value)}
              maxWidth="500px"
              placeholder="レシピ名で検索"
            />
          </FormControl>
          <FormControl flex={{ base: "1", md: "2" }} maxW={{ md: "250px" }}>
            <Input
              value={ingredientFilterText}
              onChange={(e) => handleIngredientFilter(e.target.value)}
              placeholder="材料名で検索"
            />
          </FormControl>
          <Checkbox
            isChecked={filterFavorite}
            onChange={(e) => handleFilterFavorite(e.target.checked)}
          >
            お気に入りのみ表示
          </Checkbox>
        </Flex>

        <Flex
          direction={{ base: "column", md: "row" }}
          gap={2}
          align="flex-end"
        >
          <VStack alignItems="flex-start">
            <Text textAlign="left">作成日範囲</Text>
            <HStack spacing={0}>
              <FormControl>
                <Input
                  type="date"
                  value={dateFilter.createDateFrom}
                  onChange={(e) =>
                    handleDateFilter("createDateFrom", e.target.value)
                  }
                  placeholder="開始日"
                />
              </FormControl>

              <Text>～</Text>

              <FormControl>
                <Input
                  type="date"
                  value={dateFilter.createDateTo}
                  onChange={(e) =>
                    handleDateFilter("createDateTo", e.target.value)
                  }
                  placeholder="終了日"
                />
              </FormControl>
            </HStack>
          </VStack>
          <VStack alignItems="normal">
            <Text textAlign="left">更新日範囲</Text>
            <HStack spacing={0}>
              <FormControl>
                <Input
                  type="date"
                  value={dateFilter.updateDateFrom}
                  onChange={(e) =>
                    handleDateFilter("updateDateFrom", e.target.value)
                  }
                  placeholder="開始日"
                />
              </FormControl>

              <Text>～</Text>

              <FormControl>
                <Input
                  type="date"
                  value={dateFilter.updateDateTo}
                  onChange={(e) =>
                    handleDateFilter("updateDateTo", e.target.value)
                  }
                  placeholder="終了日"
                />
              </FormControl>
            </HStack>
          </VStack>
        </Flex>

        <Divider />

        <HStack>
          <Button colorScheme="blue" size="md" onClick={loadRecipeDetails}>
            検索
          </Button>
          <Button colorScheme="gray" size="md" onClick={removeFilter}>
            検索条件をクリア
          </Button>
        </HStack>

        <HStack>
          <Button
            colorScheme="green"
            size="md"
            onClick={() => navigate("/recipes/new", { replace: true })}
          >
            新規作成
          </Button>
        </HStack>

        <Divider />

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
      </VStack>
    </>
  );
};
