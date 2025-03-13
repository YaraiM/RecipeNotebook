import { Box, Flex, Text } from "@chakra-ui/react";
import { useAuthStore } from "../stores/use-auth-store";
import { useNavigate } from "react-router-dom";

export const NavigationBar = () => {
  const { logout } = useAuthStore();
  const navigate = useNavigate();

  return (
    <Box backgroundColor="blue" p={5} mb={5}>
      <Flex justifyContent="space-between" alignItems="center">
        <Text
          color="white"
          fontSize="xl"
          fontWeight="bold"
          cursor="pointer"
          onClick={() => navigate("/recipes", { replace: true })}
        >
          RecipeNotebook
        </Text>
        <Text color="white" fontSize="md" cursor="pointer" onClick={logout}>
          ログアウト
        </Text>
      </Flex>
    </Box>
  );
};
