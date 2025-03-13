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
import { useRecipe } from "../hooks/use-recipe";

export const RecipeDetailPage = () => {
  const { recipeDetail, deleteRecipe } = useRecipe();

  const navigate = useNavigate();

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
                navigate(`/recipes/${recipeDetail.recipe.id}/update`, {
                  replace: true,
                })
              }
            >
              レシピを編集する
            </Button>
            <Button
              colorScheme="red"
              size="md"
              onClick={async () => {
                await deleteRecipe(recipeDetail);
                navigate("/recipes", { replace: true });
              }}
            >
              レシピを削除する
            </Button>
            <Button
              variant="outline"
              size="md"
              onClick={() => navigate("/recipes", { replace: true })}
            >
              レシピ一覧に戻る
            </Button>
          </HStack>
        </VStack>
      </Box>
    </>
  );
};
