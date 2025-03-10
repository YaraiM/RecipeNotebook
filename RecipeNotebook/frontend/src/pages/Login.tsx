import {
  Box,
  Container,
  Card,
  CardBody,
  Heading,
  Alert,
  AlertIcon,
  VStack,
  FormControl,
  FormLabel,
  Input,
  Button,
} from "@chakra-ui/react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../stores/use-auth-store";

export const Login = () => {
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [errorMessage, setErrorMessage] = useState<string>("");
  const navigate = useNavigate();

  const { loginUser } = useAuthStore();

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    const loginSuccess = await loginUser(username, password);
    if (loginSuccess) {
      navigate("/recipes/new");
    } else {
      setErrorMessage(
        "ログインに失敗しました。ユーザー名またはパスワードを確認してください。",
      );
    }
  };

  const hundleGuestLogin = async () => {
    const loginSuccess = await loginUser("user", "user_password");
    if (loginSuccess) {
      navigate("/recipes");
    } else {
      setErrorMessage("ゲストログインに失敗しました。");
    }
  };

  return (
    <Box bg="gray.100" minH="100vh" display="flex" alignItems="center">
      <Container maxW="md">
        <Card shadow="md" borderRadius="md">
          <CardBody p={6}>
            <Heading as="h3" size="lg" textAlign="center" mb={4}>
              RecipeNotebook
            </Heading>

            {errorMessage && (
              <Alert status="error" mb={4}>
                <AlertIcon />
                {errorMessage}
              </Alert>
            )}

            <form onSubmit={handleSubmit}>
              <VStack spacing={4}>
                <FormControl>
                  <FormLabel>ユーザー名</FormLabel>
                  <Input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="ユーザー名を入力"
                  />
                </FormControl>

                <FormControl>
                  <FormLabel>パスワード</FormLabel>
                  <Input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="パスワードを入力"
                  />
                </FormControl>

                <Button type="submit" colorScheme="blue" width="full" mt={4}>
                  ログイン
                </Button>
              </VStack>
            </form>

            <Button
              onClick={hundleGuestLogin}
              colorScheme="yellow"
              width="full"
              mt={4}
            >
              ゲストログインはこちらから
            </Button>
          </CardBody>
        </Card>
      </Container>
    </Box>
  );
};
