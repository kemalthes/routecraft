import { createBrowserRouter } from "react-router-dom";
import { AuthPage } from "./pages/AuthPage";
import { AdminPage } from "./pages/AdminPage";
import { CreateRoutePage } from "./pages/CreateRoutePage";
import { HomePage } from "./pages/HomePage";
import { MyRoutesPage } from "./pages/MyRoutesPage";
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
    path: "/my-routes",
    element: <MyRoutesPage />,
  },
  {
    path: "/admin",
    element: <AdminPage />,
  },
  {
    path: "/auth",
    element: <AuthPage />,
  },
]);
