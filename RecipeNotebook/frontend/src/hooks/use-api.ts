import { useCallback } from "react";
import { useAuthStore } from "../stores/use-auth-store";

interface FetchOptions extends RequestInit {
  withAuth?: boolean;
}

export const useApi = () => {
  const { csrfToken, csrfHeaderName, isAuthenticated, logout } = useAuthStore();

  const fetchWithAuth = useCallback(
    async (url: string, options: FetchOptions = {}) => {
      const { withAuth = true, headers = {}, ...restOptions } = options;

      const requestHeaders: Record<string, string> = {
        ...(headers as Record<string, string>),
      };

      if (withAuth && csrfToken && csrfHeaderName) {
        requestHeaders[csrfHeaderName] = csrfToken;
      }

      try {
        const response = await fetch(url, {
          ...restOptions,
          headers: requestHeaders,
          credentials: withAuth ? "include" : "same-origin",
        });

        if (response.status === 401 && isAuthenticated) {
          logout();
          return {
            error: "セッションが切れました。再度ログインしてください。",
          };
        }

        let data;
        try {
          data = await response.json();
        } catch {
          return response.ok
            ? { data: null }
            : { error: `エラーが発生しました：${response.status}` };
        }

        return response.ok
          ? { data }
          : {
              error: data.message || `エラーが発生しました：${response.status}`,
            };
      } catch (error) {
        console.error("API呼び出しエラー：", error);
        return { error: "通信エラーが発生しました" };
      }
    },
    [csrfToken, csrfHeaderName, isAuthenticated, logout],
  );
  return { fetchWithAuth };
};
