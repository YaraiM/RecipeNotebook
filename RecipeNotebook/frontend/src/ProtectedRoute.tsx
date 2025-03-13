import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "./stores/use-auth-store";

export const ProtectedRoute = () => {
  const isAuthenticated = useAuthStore((state) => state.checkSessionExpiry());

  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};
