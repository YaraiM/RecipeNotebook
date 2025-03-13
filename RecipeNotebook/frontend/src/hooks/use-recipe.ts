import { useCallback, useEffect, useState } from "react";
import { RecipeDetail } from "../types/RecipeDetail";
import { useAuthStore } from "../stores/use-auth-store";
import { DateFilter } from "../types/DateFilter";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { RecipeState } from "../types/RecipeState";
import { Ingredient } from "../types/Ingredient";
import { Instruction } from "../types/Instruction";
import { Recipe } from "../types/Recipe";
import { RecipeDetailWithImageData } from "../types/RecipeDetailWithImageData";

export const useRecipe = () => {
  const { csrfToken, csrfHeaderName } = useAuthStore();

  const { id } = useParams();
  const pathname = useLocation().pathname;

  const recipeDetailState = {
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
  };

  const [recipe, setRecipe] = useState<RecipeState>({
    id: 0,
    userId: 0,
    name: "",
    recipeSource: "",
    servings: "",
    remark: "",
    favorite: false,
    image: undefined,
    imageSelected: false,
  });

  const [ingredients, setIngredients] = useState<Ingredient[]>([
    { name: "", quantity: "", arrange: false },
  ]);

  const [instructions, setInstructions] = useState<Instruction[]>([
    { stepNumber: 1, content: "", arrange: false },
  ]);

  const [recipeDetail, setRecipeDetail] =
    useState<RecipeDetail>(recipeDetailState);

  const [recipeDetails, setRecipeDetails] = useState<RecipeDetail[]>([
    recipeDetailState,
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

  // レシピ一覧・検索の処理
  useEffect(() => {
    if (id) return;
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
  }, [id]);

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

  const handleRecipeFilter = (filterText: string) => {
    handleFilterWords(filterText, setRecipeFilterText, setRecipeFilterWords);
  };

  const handleIngredientFilter = (filterText: string) => {
    handleFilterWords(
      filterText,
      setIngredientFilterText,
      setIngredientFilterWords,
    );
  };

  const handleFilterFavorite = (favorite: boolean) => {
    setFilterFavorite(favorite);
  };

  const handleDateFilter = <Key extends keyof DateFilter>(
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
    params.append("updateDateTo", dateFilter.updateDateTo);

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

  const toggleFavorite = async (recipeDetail: RecipeDetail) => {
    const recipeId = recipeDetail.recipe.id;
    const newFavoriteStatus = !recipeDetail.recipe.favorite;

    const response = await fetch(`/api/recipes/${recipeId}/favorite`, {
      method: "PATCH",
      credentials: "include",
      headers: {
        [csrfHeaderName]: csrfToken,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        favorite: newFavoriteStatus,
      }),
    });

    if (!response.ok) {
      const errorMessage = response.toString(); // ここのエラーメッセージはJSONではなくプレーンテキスト
      alert(errorMessage);
      throw new Error(errorMessage);
    }

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
  };

  // レシピ詳細画面およびレシピ更新画面の初期情報取得
  useEffect(() => {
    const getRecipeDetail = async () => {
      if (!id) return;

      const response = await fetch(`http://localhost:8080/api/recipes/${id}`, {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      });

      const responseJson = await response.json();
      if (!response.ok) {
        throw new Error(responseJson.message);
      }

      if (pathname.includes("update")) {
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
      } else {
        setRecipeDetail(responseJson);
      }
    };

    getRecipeDetail();
  }, [id, pathname]);

  // レシピの新規登録・更新
  const formTitle = window.location.pathname.includes("new")
    ? "レシピ新規作成フォーム"
    : "レシピ更新フォーム";

  const handleRecipeChange = <Key extends keyof RecipeState>(
    field: Key,
    value: RecipeState[Key],
  ) => {
    setRecipe((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      handleRecipeChange("image", file);
      handleRecipeChange("imageSelected", true);
    }
  };

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

  const addIntruction = () => {
    setInstructions([
      ...instructions,
      { stepNumber: instructions.length + 1, content: "", arrange: false },
    ]);
  };

  const removeInstruction = (index: number) => {
    if (instructions.length === 1) {
      return alert("調理手順は最低一つは必要です");
    }
    const newInstructions = [...instructions];
    newInstructions.splice(index, 1);
    for (let i: number = 0; i < newInstructions.length; i++)
      newInstructions[i].stepNumber = i + 1;
    setInstructions(newInstructions);
  };

  const handleInstructionsChange = <Key extends keyof Instruction>(
    index: number,
    field: Key,
    value: Instruction[Key],
  ) => {
    const newInstructions = [...instructions];
    newInstructions[index][field] = value;
    setInstructions(newInstructions);
  };

  const handleFormSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
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

    // 画像データをBase64に変換
    const convertToBase64 = (file: File) => {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = (error) => reject(error);
      });
    };

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

    try {
      const uri = pathname.includes("new")
        ? "http://localhost:8080/api/recipes"
        : `http://localhost:8080/api/recipes/${id}`;

      const method = pathname.includes("new") ? "POST" : "PATCH";

      const response = await fetch(uri, {
        method: method,
        credentials: "include",
        headers: {
          [csrfHeaderName]: csrfToken,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(recipeDetailWithImageData),
      });

      const responseJson = await response.json();
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

      const recipeDetail: RecipeDetail = responseJson;
      navigate(`/recipes/${recipeDetail.recipe.id}`, { replace: true });
    } catch (error) {
      alert(error);
    }
  };

  // レシピの削除
  const deleteRecipe = async (recipeDetail: RecipeDetail) => {
    const recipeId = recipeDetail.recipe.id;
    const response = await fetch(`/api/recipes/${recipeId}`, {
      method: "DELETE",
      credentials: "include",
      headers: {
        [csrfHeaderName]: csrfToken,
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      const errorMessage = response.toString(); // ここのエラーメッセージはJSONではなくプレーンテキスト
      alert(errorMessage);
      throw new Error(errorMessage);
    }

    setRecipeDetails((prevRecipeDetails) =>
      prevRecipeDetails.filter(
        (recipeDetail) => recipeDetail.recipe.id !== recipeId,
      ),
    );

    alert(`レシピ${recipeDetail.recipe.name}を削除しました`);
  };

  return {
    recipe,
    ingredients,
    setIngredients,
    instructions,
    setInstructions,
    recipeDetail,
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
    deleteRecipe,
  };
};
