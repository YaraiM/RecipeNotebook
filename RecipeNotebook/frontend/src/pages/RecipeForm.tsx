import { Ingredient } from "../types/Ingredient";
import { useEffect, useState } from "react";
import { Instruction } from "../types/Instruction";
import { Recipe } from "../types/Recipe";
import { RecipeDetailWithImageData } from "../types/RecipeDetailWithImageData";
import { Box, Divider, Heading, VStack } from "@chakra-ui/react";
import { RecipeState } from "../types/RecipeState";
import { RecipeInfo } from "../recipe/RecipeInfo";
import { IngredientList } from "../recipe/IngredientList";
import { FormActions } from "../recipe/FormActions";
import { InstructionList } from "../recipe/InstructionList";
import { useAuthStore } from "../stores/use-auth-store";

export const RecipeForm = () => {
  // 認証情報
  const { csrfToken, csrfHeaderName } = useAuthStore();

  // レシピの状態管理（フォームに基づく項目）
  const [recipe, setRecipe] = useState<RecipeState>({
    id: 0,
    userId: 0,
    name: "",
    recipeSource: "",
    servings: "",
    remark: "",
    favorite: false,
    imageSelected: false,
  });

  // 材料の状態管理（フォームに基づく項目）
  const [ingredients, setIngredients] = useState<Ingredient[]>([
    { name: "", quantity: "", arrange: false },
  ]);

  // 調理手順の状態管理（フォームに基づく項目）
  const [instructions, setInstructions] = useState<Instruction[]>([
    { stepNumber: 1, content: "", arrange: false },
  ]);

  // 新規作成、更新に応じてフォームタイトルを変更
  const formTitle = window.location.pathname.includes("new")
    ? "レシピ新規作成フォーム"
    : "レシピ更新フォーム";

  // 更新ページの場合は初回マウント時にレシピデータを入力
  useEffect(() => {
    (async () => {
      const url = window.location.pathname;
      if (url.includes("update")) {
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

        const updateRecipeState: RecipeState = {
          id: responseJson.recipe.id,
          userId: responseJson.recipe.userId,
          name: responseJson.recipe.name || "",
          recipeSource: responseJson.recipe.recipeSource || "",
          servings: responseJson.recipe.servings || "",
          remark: responseJson.recipe.remark || "",
          favorite: responseJson.recipe.favorite || false,
          imageSelected: false,
        };
        setRecipe(updateRecipeState);

        const updateIngredients = responseJson.ingredients;
        setIngredients(updateIngredients);

        const updateInstructions = responseJson.instructions;
        setInstructions(updateInstructions);
      }
    })();
  }, []);

  // 画像データをBase64に変換
  const convertToBase64 = (file: File) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = (error) => reject(error);
    });
  };

  // レシピ情報の更新処理
  const handleRecipeChange = <Key extends keyof RecipeState>(
    field: Key,
    value: RecipeState[Key],
  ) => {
    setRecipe((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  // フォームの送信処理
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const newRecipe: Recipe = {
      id: recipe.id,
      userId: recipe.userId,
      name: recipe.name,
      recipeSource: recipe.recipeSource,
      servings: recipe.servings,
      remark: recipe.remark,
      favorite: recipe.favorite,
    };

    const newIngredients: Ingredient[] = ingredients;

    const newInstructions: Instruction[] = instructions;

    let base64ImageData = null;
    if (recipe.image) {
      base64ImageData = await convertToBase64(recipe.image);
    }

    const recipeDetailWithImageData: RecipeDetailWithImageData = {
      recipeDetail: {
        recipe: newRecipe,
        ingredients: newIngredients,
        instructions: newInstructions,
      },
      imageData: base64ImageData,
    };

    // 新規作成画面、更新画面に応じて処理を変更
    const recipeId = window.location.pathname.split("/")[2];
    const uri = window.location.pathname.includes("new")
      ? "http://localhost:8080/api/recipes"
      : `http://localhost:8080/api/recipes/${recipeId}`;

    const method = window.location.pathname.includes("new") ? "POST" : "PATCH";

    fetch(uri, {
      method: method,
      credentials: "include",
      headers: {
        [csrfHeaderName]: csrfToken,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(recipeDetailWithImageData),
    })
      .then((response) =>
        response.json().then((responseJson) => {
          if (!response.ok) {
            if (
              responseJson.message &&
              responseJson.message.includes("バリデーション")
            ) {
              throw new Error(responseJson.message);
            } else if (
              responseJson.message &&
              (responseJson.message.includes("不正なデータ形式") ||
                responseJson.message.includes("ファイルのサイズ") ||
                responseJson.message.includes("画像ファイルのみ"))
            ) {
              throw new Error(responseJson.message);
            } else {
              throw new Error("予期しないエラーが発生しました");
            }
          }
          return responseJson;
        }),
      )
      .then((recipeDetail) => {
        const recipeId = recipeDetail.recipe.id;
        window.location.href = `/recipes/${recipeId}`;
      })
      .catch((errorMessage) => {
        alert(errorMessage);
      });
  };

  return (
    <Box as="form" onSubmit={handleSubmit}>
      <VStack spacing={6} align="stretch">
        <Heading as="h1" size="xl" textAlign="center" mb={4}>
          {formTitle}
        </Heading>

        <RecipeInfo recipe={recipe} onChange={handleRecipeChange} />

        <Divider />

        <IngredientList
          ingredients={ingredients}
          setIngredients={setIngredients}
        />

        <Divider />

        <InstructionList
          instructions={instructions}
          setInstructions={setInstructions}
        />

        <FormActions />
      </VStack>
    </Box>
  );
};
