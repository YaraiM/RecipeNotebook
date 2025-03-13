import { HStack, Button } from "@chakra-ui/react";
import { useNavigate } from "react-router-dom";

export const FormActions = () => {
  const navigate = useNavigate();
  const toRecipes = () => {
    navigate("/recipes", { replace: true });
  };

  return (
    <HStack spacing={4} justify="center" pt={6}>
      <Button type="submit" colorScheme="blue" size="md">
        レシピを登録する
      </Button>
      <Button variant="outline" size="md" onClick={toRecipes}>
        レシピ一覧に戻る
      </Button>
    </HStack>
  );
};
