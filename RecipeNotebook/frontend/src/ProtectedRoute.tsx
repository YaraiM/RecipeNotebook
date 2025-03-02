import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "./stores/use-auth-store";

export const ProtectedRoute = () => {
  const { isAuthenticated } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};
