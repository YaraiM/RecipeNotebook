import { Box, Button, Heading, HStack, VStack } from "@chakra-ui/react";
import { Instruction } from "../types/Instruction";
import { AddIcon } from "@chakra-ui/icons";
import { InstructionItem } from "./InstructionItem";

type Props = {
  instructions: Instruction[];
  setInstructions: React.Dispatch<React.SetStateAction<Instruction[]>>;
};

export const InstructionList = ({ instructions, setInstructions }: Props) => {
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

  return (
    <Box>
      <HStack justify="space-between" mb={4}>
        <Heading as="h5" size="md">
          調理手順
        </Heading>
        <Button
          size="sm"
          leftIcon={<AddIcon />}
          colorScheme="blue"
          variant="outline"
          onClick={addIntruction}
        >
          手順を追加
        </Button>
      </HStack>

      <VStack spacing={3} align="stretch">
        {instructions.map((instruction, index) => (
          <InstructionItem
            key={index}
            instruction={instruction}
            index={index}
            onChange={handleInstructionsChange}
            onRemove={removeInstruction}
          />
        ))}
      </VStack>
    </Box>
  );
};
