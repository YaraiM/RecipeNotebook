import { useEffect, useState } from "react";
import { Ingredient } from "../types/Ingredient";
import { Instruction } from "../types/Instruction";
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
import { NavigationBar } from "../layout/NavigationBar";
import { useAuthStore } from "../stores/use-auth-store";
import { RecipeDetail } from "../types/RecipeDetail";

export const RecipeDetailPage = () => {
  const { csrfHeaderName, csrfToken } = useAuthStore();

  const [recipeDetail, setRecipeDetail] = useState<RecipeDetail>({
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
  });

  const navigate = useNavigate();

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

      setRecipeDetail(responseJson);
    })();
  }, []);

  return (
    <>
      <NavigationBar />
      <Box>
        <VStack spacing={6} align="stretch">
          <Heading as="h1" size="xl" textAlign="center" mb={4}>
            {recipeDetail.recipe.name}
          </Heading>

          <Flex justify="center">
            <Box boxSize="xs">
              {recipeDetail.recipe.imagePath && (
                <Image
                  src={`http://localhost:8080${recipeDetail.recipe.imagePath}`}
                  alt={recipeDetail.recipe.name}
                  mb={0}
                />
              )}
            </Box>
          </Flex>

          <Grid templateColumns="repeat(2, 1fr)" gap={6}>
            <GridItem>
              <VStack>
                <Text fontSize="xl">情報元</Text>
                <Text fontSize="sm">{recipeDetail.recipe.recipeSource}</Text>
              </VStack>
            </GridItem>
            <GridItem>
              <VStack>
                <Text fontSize="xl">分量</Text>
                <Text fontSize="sm">{recipeDetail.recipe.servings}</Text>
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
                {recipeDetail.ingredients.map((ingredient: Ingredient) => (
                  <Tr key={ingredient.id}>
                    <Td>{ingredient.name}</Td>
                    <Td>{ingredient.quantity}</Td>
                    <Td>
                      {ingredient.arrange && <Icon as={CheckCircleIcon} />}
                    </Td>
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
                {recipeDetail.instructions.map((instruction: Instruction) => (
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
            {recipeDetail.recipe.remark}
          </Text>

          <Divider />

          <VStack spacing={1} alignItems="normal">
            <Text fontSize="sm" align="left">
              作成日：
              {recipeDetail.recipe.createdAt
                ? new Intl.DateTimeFormat("ja-JP").format(
                    new Date(recipeDetail.recipe.createdAt),
                  )
                : ""}
            </Text>
            <Text fontSize="sm" align="left">
              更新日：
              {recipeDetail.recipe.updatedAt
                ? new Intl.DateTimeFormat("ja-JP").format(
                    new Date(recipeDetail.recipe.updatedAt),
                  )
                : ""}
            </Text>
          </VStack>
          <Divider />

          <HStack spacing={4} justify="center" pt={6}>
            <Button
              colorScheme="blue"
              size="md"
              onClick={() =>
                navigate(`/recipes/${recipeDetail.recipe.id}/update`)
              }
            >
              レシピを編集する
            </Button>
            <Button
              colorScheme="red"
              size="md"
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
                      navigate("/recipes");
                    }
                    alert(`レシピ${recipeDetail.recipe.name}を削除しました`);
                  })
                  .catch((error) => {
                    console.error("レシピの削除に失敗しました", error);
                  });
                e.stopPropagation();
              }}
            >
              レシピを削除する
            </Button>
            <Button
              variant="outline"
              size="md"
              onClick={() => navigate("/recipes")}
            >
              レシピ一覧に戻る
            </Button>
          </HStack>
        </VStack>
      </Box>
    </>
  );
};
