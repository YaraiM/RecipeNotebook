import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../stores/use-auth-store";

export const useLogin = () => {
  const [errorMessage, setErrorMessage] = useState<string>("");
  const navigate = useNavigate();

  const { loginUser } = useAuthStore();

  const loginAsUser = async (username: string, password: string) => {
    const loginSuccess = await loginUser(username, password);
    if (loginSuccess) {
      navigate("/recipes");
    } else {
      setErrorMessage(
        "ログインに失敗しました。ユーザー名またはパスワードを確認してください。",
      );
    }
  };

  const loginAsGuest = async () => {
    loginAsUser("user", "user_password");
  };

  return {
    loginAsUser,
    loginAsGuest,
    errorMessage,
  };
};
