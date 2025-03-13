import { HStack, Button } from "@chakra-ui/react";
import { useNavigate } from "react-router-dom";

type Props = {
  loadRecipeDetails: () => Promise<void>;
  removeFilter: () => void;
};

export const Buttons = ({ loadRecipeDetails, removeFilter }: Props) => {
  const navigate = useNavigate();
  return (
    <>
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
    </>
  );
};
