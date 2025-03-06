import { useEffect, useState } from "react";
import { Ingredient } from "../types/Ingredient";
import { Instruction } from "../types/Instruction";
import { Recipe } from "../types/Recipe";
import {
  Box,
  VStack,
  Heading,
  Divider,
  Image,
  Text,
  Grid,
  GridItem,
  TableContainer,
  Table,
  Tbody,
  Td,
  Th,
  Thead,
  Tr,
  Icon,
  Flex,
  HStack,
  Button,
} from "@chakra-ui/react";
import { CheckCircleIcon } from "@chakra-ui/icons";
import { useNavigate } from "react-router-dom";

export const RecipeDetail = () => {
  // レシピの状態管理（フォームに基づく項目）
  const [recipe, setRecipe] = useState<Recipe>({
    id: 0,
    userId: 0,
    name: "",
    imagePath: "",
    recipeSource: "",
    servings: "",
    remark: "",
    favorite: false,
    createdAt: new Date("2000-01-01"),
    updatedAt: new Date("2000-01-01"),
  });

  // 材料の状態管理（フォームに基づく項目）
  const [ingredients, setIngredients] = useState<Ingredient[]>([
    { id: 0, name: "", quantity: "", arrange: false },
  ]);

  // 調理手順の状態管理（フォームに基づく項目）
  const [instructions, setInstructions] = useState<Instruction[]>([
    { id: 0, stepNumber: 1, content: "", arrange: false },
  ]);

  // ボタンクリック時の遷移
  const navigate = useNavigate();
  const toRecipes = () => {
    navigate("/recipes");
  };
  const toUpdate = () => {
    navigate(`/recipes/${recipe.id}/update`);
  };

  // 初回マウント時にレシピデータを入力
  useEffect(() => {
    (async () => {
      const url = window.location.pathname;
      const recipeId = url.split("/")[2];

      const response = await fetch(
        `http://localhost:8080/api/recipes/${recipeId}`,
        {
          method: "GET",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        },
      );

      const responseJson = await response.json();
      if (!response.ok) {
        throw new Error(responseJson.message);
      }

      const recipeInfo = responseJson.recipe;
      setRecipe({
        ...recipeInfo,
        createdAt: new Date(recipeInfo.createdAt),
        updatedAt: new Date(recipeInfo.updatedAt),
      });

      const ingredientsInfo = responseJson.ingredients;
      setIngredients(ingredientsInfo);

      const instructionsInfo = responseJson.instructions;
      setInstructions(instructionsInfo);
    })();
  }, []);

  console.log(recipe);

  return (
    <Box>
      <VStack spacing={6} align="stretch">
        <Heading as="h1" size="xl" textAlign="center" mb={4}>
          {recipe.name}
        </Heading>

        <Flex justify="center">
          <Box boxSize="xs">
            {recipe.imagePath && (
              <Image
                src={`http://localhost:8080${recipe.imagePath}`}
                alt={recipe.name}
                mb={0}
              />
            )}
          </Box>
        </Flex>

        <Grid templateColumns="repeat(2, 1fr)" gap={6}>
          <GridItem>
            <VStack>
              <Text fontSize="xl">情報元</Text>
              <Text fontSize="sm">{recipe.recipeSource}</Text>
            </VStack>
          </GridItem>
          <GridItem>
            <VStack>
              <Text fontSize="xl">分量</Text>
              <Text fontSize="sm">{recipe.servings}</Text>
            </VStack>
          </GridItem>
        </Grid>

        <Divider />

        <TableContainer>
          <Table variant="simple">
            <Thead>
              <Tr>
                <Th>材料</Th>
                <Th>分量</Th>
                <Th>アレンジ</Th>
              </Tr>
            </Thead>
            <Tbody>
              {ingredients.map((ingredient) => (
                <Tr key={ingredient.id}>
                  <Td>{ingredient.name}</Td>
                  <Td>{ingredient.quantity}</Td>
                  <Td>{ingredient.arrange && <Icon as={CheckCircleIcon} />}</Td>
                </Tr>
              ))}
            </Tbody>
          </Table>
        </TableContainer>

        <Divider />

        <TableContainer>
          <Table variant="simple">
            <Thead>
              <Tr>
                <Th>手順</Th>
                <Th>内容</Th>
                <Th>アレンジ</Th>
              </Tr>
            </Thead>
            <Tbody>
              {instructions.map((instruction) => (
                <Tr key={instruction.stepNumber}>
                  <Td>{instruction.stepNumber}</Td>
                  <Td>{instruction.content}</Td>
                  <Td alignItems="center">
                    {instruction.arrange && <Icon as={CheckCircleIcon} />}
                  </Td>
                </Tr>
              ))}
            </Tbody>
          </Table>
        </TableContainer>

        <Divider />

        <Text fontSize="sm" align="left">
          {recipe.remark}
        </Text>

        <Divider />

        <VStack spacing={1} alignItems="normal">
          <Text fontSize="sm" align="left">
            作成日：{recipe.createdAt?.toLocaleDateString()}
          </Text>
          <Text fontSize="sm" align="left">
            更新日：{recipe.updatedAt?.toLocaleDateString()}
          </Text>
        </VStack>
        <Divider />

        <HStack spacing={4} justify="center" pt={6}>
          <Button colorScheme="blue" size="md" onClick={toUpdate}>
            レシピを編集する
          </Button>
          <Button colorScheme="red" size="md">
            レシピを削除する
          </Button>
          <Button variant="outline" size="md" onClick={toRecipes}>
            レシピ一覧に戻る
          </Button>
        </HStack>
      </VStack>
    </Box>
  );
};
