import { HStack, Button } from "@chakra-ui/react";

export const FormActions = () => {
  return (
    <HStack spacing={4} justify="center" pt={6}>
      <Button type="submit" colorScheme="blue" size="md">
        レシピを登録する
      </Button>
      <Button variant="outline" size="md">
        レシピ一覧に戻る
      </Button>
    </HStack>
  );
};
