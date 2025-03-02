import {
  Box,
  HStack,
  FormControl,
  Checkbox,
  Textarea,
  IconButton,
  Input,
} from "@chakra-ui/react";
import { Instruction } from "../types/Instruction";
import { DeleteIcon } from "@chakra-ui/icons";

type Props = {
  instruction: Instruction;
  index: number;
  onChange: <Key extends keyof Instruction>(
    index: number,
    field: Key,
    value: Instruction[Key],
  ) => void;
  onRemove: (index: number) => void;
};

export const InstructionItem = ({
  instruction,
  index,
  onChange,
  onRemove,
}: Props) => {
  return (
    <HStack spacing={3} align="center">
      <FormControl flex="0">
        <Input
          isDisabled={true}
          w="40px"
          h="40px"
          bg="gray.100"
          borderRadius="md"
          defaultValue={index + 1}
          onChange={(e) =>
            onChange(index, "stepNumber", Number(e.target.value))
          }
        />
      </FormControl>

      <FormControl flex="5">
        <Textarea
          placeholder="調理内容"
          value={instruction.content}
          onChange={(e) => onChange(index, "content", e.target.value)}
          size="sm"
          rows={2}
        />
      </FormControl>

      <Box pt={2}>
        <Checkbox
          isChecked={instruction.arrange}
          onChange={(e) => onChange(index, "arrange", e.target.checked)}
        >
          アレンジ
        </Checkbox>
      </Box>

      <IconButton
        icon={<DeleteIcon />}
        colorScheme="red"
        aria-label="手順を削除"
        onClick={() => onRemove(index)}
        size="sm"
      />
    </HStack>
  );
};
