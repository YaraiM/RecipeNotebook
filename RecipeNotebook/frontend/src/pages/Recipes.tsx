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
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { DateFilter } from "../types/DateFilter";
import { RecipeDetail } from "../types/RecipeDetail";
import { useAuthStore } from "../stores/use-auth-store";

export const Recipes = () => {
  const { csrfToken, csrfHeaderName } = useAuthStore();

  const [recipeDetails, setRecipeDetails] = useState<RecipeDetail[]>([
    {
      recipe: {
        id: 0,
        userId: 0,
        name: "",
        imagePath: "",
        recipeSource: "",
        servings: "",
        remark: "",
        favorite: false,
        createdAt: new Date(),
        updatedAt: new Date(),
      },
      ingredients: [],
      instructions: [],
    },
  ]);

  const [recipeFilterText, setRecipeFilterText] = useState<string>("");
  const [recipeFilterWords, setRecipeFilterWords] = useState<string[]>([]);

  const [ingredientFilterText, setIngredientFilterText] = useState<string>("");
  const [ingredientFilterWords, setIngredientFilterWords] = useState<string[]>(
    [],
  );

  const [dateFilter, setDateFilter] = useState<DateFilter>({
    createDateFrom: "",
    createDateTo: "",
    updateDateFrom: "",
    updateDateTo: "",
  });

  const [filterFavorite, setFilterFavorite] = useState<boolean>();

  const navigate = useNavigate();

  const handleFilterWords = (
    filterText: string,
    setTextState: (value: React.SetStateAction<string>) => void,
    setWordsState: (value: React.SetStateAction<string[]>) => void,
  ) => {
    setTextState(filterText);

    const normalizedSpace = filterText.replace(/\s+/g, " ").trim();
    const convertToWordArray = normalizedSpace.split(" ");
    setWordsState(convertToWordArray);
  };

  const hundleDateFilter = <Key extends keyof DateFilter>(
    field: Key,
    value: DateFilter[Key],
  ) =>
    setDateFilter((prev) => ({
      ...prev,
      [field]: value,
    }));

  const removeFilter = () => {
    setRecipeFilterText("");
    setRecipeFilterWords([]);
    setIngredientFilterText("");
    setIngredientFilterWords([]);
    setFilterFavorite(false);
    setDateFilter({
      createDateFrom: "",
      createDateTo: "",
      updateDateFrom: "",
      updateDateTo: "",
    });
  };

  const loadRecipeDetails = useCallback(async () => {
    const params = new URLSearchParams();
    recipeFilterWords.forEach((word) => params.append("recipeNames", word));
    ingredientFilterWords.forEach((word) =>
      params.append("ingredientNames", word),
    );
    if (filterFavorite) {
      params.append("favoriteRecipe", filterFavorite.toString());
    }
    params.append("createDateFrom", dateFilter.createDateFrom);
    params.append("createDateTo", dateFilter.createDateTo);
    params.append("updateDateFrom", dateFilter.updateDateFrom);
    params.append("updateDateFrom", dateFilter.updateDateTo);

    const response = await fetch(
      `http://localhost:8080/api/recipes?${params.toString()}`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      },
    );

    const recipeDetails = await response.json();
    if (!response.ok) {
      alert(recipeDetails.message);
      throw new Error(recipeDetails.message);
    }

    setRecipeDetails(recipeDetails);
  }, [recipeFilterWords, ingredientFilterWords, filterFavorite, dateFilter]);

  useEffect(() => {
    const fetchAllRecipes = async () => {
      const response = await fetch("http://localhost:8080/api/recipes", {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      });

      const recipeDetails = await response.json();
      if (!response.ok) {
        throw new Error(recipeDetails.message);
      }

      setRecipeDetails(recipeDetails);
    };

    fetchAllRecipes();
  }, []);

  return (
    <VStack spacing={3} align="stretch">
      <Flex direction={{ base: "column", md: "row" }} gap={2} align="flex-end">
        <FormControl flex={{ base: "1", md: "2" }} maxW={{ md: "250px" }}>
          <Input
            value={recipeFilterText}
            onChange={(e) =>
              handleFilterWords(
                e.target.value,
                setRecipeFilterText,
                setRecipeFilterWords,
              )
            }
            maxWidth="500px"
            placeholder="レシピ名で検索"
          />
        </FormControl>
        <FormControl flex={{ base: "1", md: "2" }} maxW={{ md: "250px" }}>
          <Input
            value={ingredientFilterText}
            onChange={(e) =>
              handleFilterWords(
                e.target.value,
                setIngredientFilterText,
                setIngredientFilterWords,
              )
            }
            placeholder="材料名で検索"
          />
        </FormControl>
        <Box flex="1">
          <Checkbox
            isChecked={filterFavorite}
            onChange={(e) => setFilterFavorite(e.target.checked)}
          >
            お気に入りのみ表示
          </Checkbox>
        </Box>
      </Flex>

      <Flex direction={{ base: "column", md: "row" }} gap={2} align="flex-end">
        <VStack alignItems="flex-start">
          <Text textAlign="left">作成日範囲</Text>
          <HStack spacing={0}>
            <FormControl>
              <Input
                type="date"
                value={dateFilter.createDateFrom}
                onChange={(e) =>
                  hundleDateFilter("createDateFrom", e.target.value)
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
                  hundleDateFilter("createDateTo", e.target.value)
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
                  hundleDateFilter("updateDateFrom", e.target.value)
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
                  hundleDateFilter("updateDateTo", e.target.value)
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
          onClick={() => navigate("/recipes/new")}
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
            onClick={() => navigate(`/recipes/${recipeDetail.recipe.id}`)}
            cursor="pointer"
          >
            <CardHeader py={1}>
              <HStack>
                <Box
                  fontSize="2xl"
                  cursor="pointer"
                  onClick={(e) => {
                    const recipeId = recipeDetail.recipe.id;
                    const newFavoriteStatus = !recipeDetail.recipe.favorite;
                    fetch(`/api/recipes/${recipeId}/favorite`, {
                      method: "PATCH",
                      credentials: "include",
                      headers: {
                        [csrfHeaderName]: csrfToken,
                        "Content-Type": "application/json",
                      },
                      body: JSON.stringify({
                        favorite: newFavoriteStatus,
                      }),
                    })
                      .then((response) => {
                        if (response.ok) {
                          setRecipeDetails((prevRecipeDetails) =>
                            prevRecipeDetails.map((recipeDetail) =>
                              recipeDetail.recipe.id === recipeId
                                ? {
                                    ...recipeDetail,
                                    recipe: {
                                      ...recipeDetail.recipe,
                                      favorite: newFavoriteStatus,
                                    },
                                  }
                                : recipeDetail,
                            ),
                          );
                        }
                      })
                      .catch((error) => {
                        console.error("お気に入り更新に失敗しました", error);
                      });
                    e.stopPropagation();
                  }}
                >
                  {recipeDetail.recipe.favorite ? "★" : "☆"}
                </Box>
                <Box
                  fontSize="2xl"
                  cursor="pointer"
                  onClick={(e) => {
                    navigate(`/recipes/${recipeDetail.recipe.id}/update`);
                    e.stopPropagation();
                  }}
                >
                  ✎
                </Box>
                <Box
                  fontSize="3xl"
                  cursor="pointer"
                  onClick={(e) => {
                    const recipeId = recipeDetail.recipe.id;
                    fetch(`/api/recipes/${recipeId}`, {
                      method: "DELETE",
                      credentials: "include",
                      headers: {
                        [csrfHeaderName]: csrfToken,
                        "Content-Type": "application/json",
                      },
                    })
                      .then((response) => {
                        if (response.ok) {
                          setRecipeDetails((prevRecipeDetails) =>
                            prevRecipeDetails.filter(
                              (recipeDetail) =>
                                recipeDetail.recipe.id !== recipeId,
                            ),
                          );
                        }
                        alert(
                          `レシピ${recipeDetail.recipe.name}を削除しました`,
                        );
                      })
                      .catch((error) => {
                        console.error("レシピの削除に失敗しました", error);
                      });
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
  );
};
