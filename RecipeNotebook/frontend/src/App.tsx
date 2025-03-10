// import { useState } from "react";
// import reactLogo from "./assets/react.svg";
// import viteLogo from "/vite.svg";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import "./App.css";
import { RecipeForm } from "./pages/RecipeForm";
import { Login } from "./pages/Login";
import { useAuthStore } from "./stores/use-auth-store";
import { ProtectedRoute } from "./ProtectedRoute";
import { RecipeDetailPage } from "./pages/RecipeDetailPage";
import { Recipes } from "./pages/Recipes";
import { useCallback, useEffect } from "react";
import { ActivityTracker } from "./ActivityTracker";

const App = () => {
  const { checkSessionExpiry } = useAuthStore();

  // useEffectでcheckSessionExpiraryが何度も再定義されないようメモ化
  const checkAuth = useCallback(() => {
    checkSessionExpiry();
  }, [checkSessionExpiry]);

  useEffect(() => {
    const interval = setInterval(checkAuth, 1000);

    return () => clearInterval(interval);
  }, [checkAuth]);
  const isAuthenticated = useAuthStore((state) => state.checkSessionExpiry());

  return (
    <>
      <ActivityTracker />
      <BrowserRouter>
        <Routes>
          <Route
            path="/login"
            element={
              isAuthenticated ? <Navigate to="/recipes" replace /> : <Login />
            }
          />
          <Route element={<ProtectedRoute />}>
            <Route path="/recipes" element={<Recipes />} />
            <Route path="/recipes/new" element={<RecipeForm />} />
            <Route path="/recipes/:id" element={<RecipeDetailPage />} />
            <Route path="/recipes/:id/update" element={<RecipeForm />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
};

export default App;
