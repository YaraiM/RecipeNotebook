import { Box, Button, Heading, HStack, VStack } from "@chakra-ui/react";
import { Instruction } from "../../types/Instruction";
import { AddIcon } from "@chakra-ui/icons";
import { InstructionItem } from "./InstructionItem";

type Props = {
  instructions: Instruction[];
  setInstructions: React.Dispatch<React.SetStateAction<Instruction[]>>;
  addIntruction: () => void;
  removeInstruction: (index: number) => void;
  handleInstructionsChange: <Key extends keyof Instruction>(
    index: number,
    field: Key,
    value: Instruction[Key],
  ) => void;
};

export const InstructionList = ({
  instructions,
  addIntruction,
  removeInstruction,
  handleInstructionsChange,
}: Props) => {
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
