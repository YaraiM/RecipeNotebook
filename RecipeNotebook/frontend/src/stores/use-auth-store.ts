import { create } from "zustand";
import { persist } from "zustand/middleware";

type AuthState = {
  // 状態
  isAuthenticated: boolean;
  username: string;
  csrfToken: string;
  csrfHeaderName: string;

  // アクション
  setCredentials: (username: string) => void;
  setCsrfInfo: (token: string, headerName: string) => void;
  login: () => void;
  logout: () => void;

  // API
  fetchCsrfToken: () => Promise<boolean>;
  loginUser: (username: string, password: string) => Promise<boolean>;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      isAuthenticated: false,
      username: "",
      csrfToken: "",
      csrfHeaderName: "",

      setCredentials: (username: string) => set({ username }),
      setCsrfInfo: (token: string, headerName: string) =>
        set({ csrfToken: token, csrfHeaderName: headerName }),
      login: () => set({ isAuthenticated: true }),
      logout: () =>
        set({
          isAuthenticated: false,
          username: "",
          csrfToken: "",
          csrfHeaderName: "",
        }),

      fetchCsrfToken: async () => {
        try {
          const response = await fetch("http://localhost:8080/csrf-token", {
            method: "GET",
            credentials: "include",
          });

          if (!response.ok) throw new Error("CSRFトークンの取得に失敗しました");

          const { token, headerName } = await response.json();
          get().setCsrfInfo(token, headerName);
          return true;
        } catch (error) {
          console.error("CSRFトークン取得エラー", error);
          return false;
        }
      },

      // 以下の処理には、fetchCsrfToken,setCredentials,loginが内包されているので、ログイン時はこれを使えばよい
      loginUser: async (username: string, password: string) => {
        const csrfSuccess = await get().fetchCsrfToken();
        if (!csrfSuccess) return false;

        try {
          const response = await fetch("http://localhost:8080/login", {
            method: "POST",
            credentials: "include",
            headers: {
              "Content-Type": "application/x-www-form-urlencoded",
              [get().csrfHeaderName]: get().csrfToken,
            },
            body: new URLSearchParams({ username, password }),
          });

          if (!response.ok) return false;

          get().setCredentials(username);
          get().login();

          // ログイン後にCSRFトークンを再取得し、LocalStorageに保存
          const csrfSuccess = await get().fetchCsrfToken();
          if (!csrfSuccess) return false;

          return true;
        } catch (error) {
          console.error("ログインエラー：", error);
          return false;
        }
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        username: state.username,
        csrfToken: state.csrfToken,
        csrfHeaderName: state.csrfHeaderName,
      }),
    },
  ),
);
