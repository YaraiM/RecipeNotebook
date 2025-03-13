import { useEffect } from "react";
import { useAuthStore } from "./stores/use-auth-store";

export const ActivityTracker = () => {
  const { isAuthenticated, resetTimeout } = useAuthStore();

  useEffect(() => {
    if (!isAuthenticated) return;

    // セットアップ（マウント時および依存配列変更時に実行）
    const events = [
      "mousedown",
      "mousemove",
      "keypress",
      "scroll",
      "touchstart",
    ];
    const handleUserActivity = () => resetTimeout();
    events.forEach((event) =>
      window.addEventListener(event, handleUserActivity),
    );

    // クリーンアップ関数（アンマウント時に実行）
    return () =>
      events.forEach((event) =>
        window.removeEventListener(event, handleUserActivity),
      );
  }, [isAuthenticated, resetTimeout]);

  return null;
};
