import { Box, HStack, Button } from "@chakra-ui/react";
import { useRecipeForm } from "../hooks/use-recipe-form";
import { InstructionItemForm } from "./InstructionItem";
import { Instruction } from "../types/Instruction";

type Props = {
  isUpdate: boolean;
  instructions: Instruction[];
  setInstructions: React.Dispatch<React.SetStateAction<Instruction[]>>;
};

export const InstructionsForm = ({
  isUpdate,
  instructions,
  setInstructions,
}: Props) => {
  const { addInstructionForm } = useRecipeForm();

  if (isUpdate) {
    // レシピ詳細情報を取得するAPIをフェッチ
    // ingredientsのlengthだけaddIngredient
    // 取得したレシピ詳細情報をフォームに入力（setRecipeNameなど）
  }

  return (
    <>
      <Box>
        <Box>
          <HStack spacing="8px">
            <Box>調理手順</Box>
            <Button onClick={() => addInstructionForm}>手順を追加</Button>
          </HStack>
        </Box>
        <InstructionItemForm />
      </Box>
    </>
  );
};
