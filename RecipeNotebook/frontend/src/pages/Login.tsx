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
import { useLogin } from "../hooks/use-login";

export const Login = () => {
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");

  const { loginAsUser, loginAsGuest, errorMessage } = useLogin();

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

              <Button
                onClick={() => loginAsUser(username, password)}
                colorScheme="blue"
                width="full"
                mt={4}
              >
                ログイン
              </Button>
            </VStack>

            <Button
              onClick={loginAsGuest}
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
