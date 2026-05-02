import { createBrowserRouter } from "react-router-dom";
import { AuthPage } from "./pages/AuthPage";
import { CreateRoutePage } from "./pages/CreateRoutePage";
import { HomePage } from "./pages/HomePage";
import { RouteDetailsPage } from "./pages/RouteDetailsPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <HomePage />,
  },
  {
    path: "/route/:id",
    element: <RouteDetailsPage />,
  },
  {
    path: "/create",
    element: <CreateRoutePage />,
  },
  {
    path: "/auth",
    element: <AuthPage />,
  },
]);
