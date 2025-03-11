import { useCallback, useEffect, useState } from "react";
import { RecipeDetail } from "../types/RecipeDetail";
import { useAuthStore } from "../stores/use-auth-store";
import { DateFilter } from "../types/DateFilter";
import { useParams } from "react-router-dom";

export const useRecipe = () => {
  const { csrfToken, csrfHeaderName } = useAuthStore();

  const { id } = useParams();

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

  // レシピ詳細画面
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

      setRecipeDetail(responseJson);
    };

    getRecipeDetail();
  }, [id]);

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
    deleteRecipe,
  };
};
