// import { useState } from "react";
// import reactLogo from "./assets/react.svg";
// import viteLogo from "/vite.svg";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import "./App.css";
import { RecipeForm } from "./pages/RecipeForm";
import { Login } from "./pages/Login";
import { useAuthStore } from "./stores/use-auth-store";
import { ProtectedRoute } from "./ProtectedRoute";

const App = () => {
  const { isAuthenticated } = useAuthStore();

  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route
            path="/login"
            element={
              isAuthenticated ? <Navigate to="/recipes" replace /> : <Login />
            }
          />
          <Route element={<ProtectedRoute />}>
            {/* <Route path="/recipes" element={<Recipes />} /> */}
            <Route path="/recipes/new" element={<RecipeForm />} />
            {/* <Route path="/recipes/:id" element={<RecipeDetail />} /> */}
            <Route path="/recipes/:id/update" element={<RecipeForm />} />
            {/* <Route path="*" element={<NotFound />} /> */}
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
};

export default App;
