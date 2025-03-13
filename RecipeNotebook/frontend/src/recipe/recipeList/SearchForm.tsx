import {
  Flex,
  FormControl,
  Input,
  Checkbox,
  VStack,
  HStack,
  Text,
} from "@chakra-ui/react";
import { DateFilter } from "../../types/DateFilter";

type Props = {
  recipeFilterText: string;
  ingredientFilterText: string;
  filterFavorite: boolean | undefined;
  dateFilter: DateFilter;
  handleRecipeFilter: (filterText: string) => void;
  handleIngredientFilter: (filterText: string) => void;
  handleFilterFavorite: (favorite: boolean) => void;
  handleDateFilter: <Key extends keyof DateFilter>(
    field: Key,
    value: DateFilter[Key],
  ) => void;
  removeFilter: () => void;
};

export const SearchForm = ({
  recipeFilterText,
  ingredientFilterText,
  filterFavorite,
  dateFilter,
  handleRecipeFilter,
  handleIngredientFilter,
  handleFilterFavorite,
  handleDateFilter,
}: Props) => {
  return (
    <>
      <Flex direction={{ base: "column", md: "row" }} gap={2} align="flex-end">
        <FormControl flex={{ base: "1", md: "2" }} maxW={{ md: "250px" }}>
          <Input
            value={recipeFilterText}
            onChange={(e) => handleRecipeFilter(e.target.value)}
            maxWidth="500px"
            placeholder="レシピ名で検索"
          />
        </FormControl>
        <FormControl flex={{ base: "1", md: "2" }} maxW={{ md: "250px" }}>
          <Input
            value={ingredientFilterText}
            onChange={(e) => handleIngredientFilter(e.target.value)}
            placeholder="材料名で検索"
          />
        </FormControl>
        <Checkbox
          isChecked={filterFavorite}
          onChange={(e) => handleFilterFavorite(e.target.checked)}
        >
          お気に入りのみ表示
        </Checkbox>
      </Flex>

      <Flex direction={{ base: "column", md: "row" }} gap={2} align="flex-end">
        <VStack alignItems="flex-start">
          <Text textAlign="left">作成日範囲</Text>
          <HStack spacing={0}>
            <FormControl>
              <Input
                type="date"
                value={dateFilter.createDateFrom}
                onChange={(e) =>
                  handleDateFilter("createDateFrom", e.target.value)
                }
                placeholder="開始日"
              />
            </FormControl>

            <Text>～</Text>

            <FormControl>
              <Input
                type="date"
                value={dateFilter.createDateTo}
                onChange={(e) =>
                  handleDateFilter("createDateTo", e.target.value)
                }
                placeholder="終了日"
              />
            </FormControl>
          </HStack>
        </VStack>
        <VStack alignItems="normal">
          <Text textAlign="left">更新日範囲</Text>
          <HStack spacing={0}>
            <FormControl>
              <Input
                type="date"
                value={dateFilter.updateDateFrom}
                onChange={(e) =>
                  handleDateFilter("updateDateFrom", e.target.value)
                }
                placeholder="開始日"
              />
            </FormControl>

            <Text>～</Text>

            <FormControl>
              <Input
                type="date"
                value={dateFilter.updateDateTo}
                onChange={(e) =>
                  handleDateFilter("updateDateTo", e.target.value)
                }
                placeholder="終了日"
              />
            </FormControl>
          </HStack>
        </VStack>
      </Flex>
    </>
  );
};
