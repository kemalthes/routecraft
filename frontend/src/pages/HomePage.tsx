import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert, Input, Layout, Tabs, message } from "antd";
import { useRoutesStore } from "../store/routes-store";
import { HomeTopBar } from "../components/home/HomeTopBar";
import { HomeInfoSider } from "../components/home/HomeInfoSider";
import { HomeRoutesSection } from "../components/home/HomeRoutesSection";
import { authApi } from "../api/auth-api";
import { searchApi } from "../api/search-api";
import type { PaginationMeta, RoutePreview } from "../types/routes";

const { Content, Footer } = Layout;
const DEFAULT_PAGE_SIZE = 12;

export const HomePage = () => {
  const navigate = useNavigate();
  const routes = useRoutesStore((state) => state.routes);
  const favorites = useRoutesStore((state) => state.favorites);
  const pagination = useRoutesStore((state) => state.pagination);
  const favoritesPagination = useRoutesStore((state) => state.favoritesPagination);
  const routesLoading = useRoutesStore((state) => state.routesLoading);
  const togglingFavoriteId = useRoutesStore((state) => state.togglingFavoriteId);
  const errorMessage = useRoutesStore((state) => state.errorMessage);
  const fetchRoutes = useRoutesStore((state) => state.fetchRoutes);
  const fetchFavorites = useRoutesStore((state) => state.fetchFavorites);
  const toggleFavorite = useRoutesStore((state) => state.toggleFavorite);

  const [searchInput, setSearchInput] = useState("");
  const [appliedSearch, setAppliedSearch] = useState("");
  const [activeTab, setActiveTab] = useState("routes");
  const [aiQuery, setAiQuery] = useState("");
  const [aiRoutes, setAiRoutes] = useState<RoutePreview[]>([]);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState<string | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  const role = localStorage.getItem("role");
  const isAuthenticated = Boolean(localStorage.getItem("accessToken"));
  const isAdmin = role === "ROLE_ADMIN" || role === "ADMIN";

  const aiPagination: PaginationMeta = useMemo(
    () => ({
      totalItems: aiRoutes.length,
      totalPages: aiRoutes.length === 0 ? 0 : 1,
      currentPage: 1,
      itemsPerPage: DEFAULT_PAGE_SIZE,
    }),
    [aiRoutes.length],
  );

  useEffect(() => {
    void fetchRoutes({ page: 1, limit: DEFAULT_PAGE_SIZE });
  }, [fetchRoutes]);

  const runSearch = async (query: string) => {
    const normalized = query.trim();
    setAppliedSearch(normalized);
    setActiveTab("routes");
    await fetchRoutes({
      page: 1,
      limit: pagination.itemsPerPage || DEFAULT_PAGE_SIZE,
      search: normalized || undefined,
    });
  };

  const runAiSearch = async (query: string) => {
    if (!isAdmin) {
      messageApi.warning("ИИ-поиск доступен только администраторам.");
      return;
    }

    const normalized = query.trim();
    if (normalized.length < 2) {
      messageApi.warning("Введите запрос минимум из 2 символов.");
      return;
    }

    try {
      setAiLoading(true);
      setAiError(null);
      const result = await searchApi.searchRoutes({ q: normalized, limit: DEFAULT_PAGE_SIZE });
      setAiRoutes(result);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : "Не удалось выполнить ИИ-поиск.";
      setAiError(messageText);
      messageApi.error(messageText);
    } finally {
      setAiLoading(false);
    }
  };

  const handlePageChange = async (page: number, pageSize: number) => {
    if (activeTab === "ai") {
      return;
    }
    if (activeTab === "favorites") {
      await fetchFavorites({ page, limit: pageSize });
      return;
    }

    await fetchRoutes({ page, limit: pageSize, search: appliedSearch || undefined });
  };

  const handleTabChange = async (key: string) => {
    if (key === "ai") {
      if (!isAdmin) {
        messageApi.warning("ИИ-поиск доступен только администраторам.");
        return;
      }
      setActiveTab(key);
      return;
    }

    if (key === "favorites") {
      if (!isAuthenticated) {
        messageApi.warning("Войдите, чтобы открыть избранное.");
        return;
      }
      setActiveTab(key);
      await fetchFavorites({ page: 1, limit: DEFAULT_PAGE_SIZE });
      return;
    }

    setActiveTab(key);
    await fetchRoutes({
      page: 1,
      limit: pagination.itemsPerPage || DEFAULT_PAGE_SIZE,
      search: appliedSearch || undefined,
    });
  };

  const handleToggleFavorite = async (routeId: string, nextLiked: boolean) => {
    if (!isAuthenticated) {
      messageApi.warning("Войдите, чтобы добавлять маршруты в избранное.");
      return;
    }
    try {
      await toggleFavorite(routeId, nextLiked);
    } catch {
      messageApi.error("Не удалось обновить избранное. Обновите страницу и попробуйте снова.");
    }
  };

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch {
      // Локальный выход важнее, если токен уже истек или был отозван.
    } finally {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("role");
      messageApi.success("Вы вышли из аккаунта");
      setActiveTab("routes");
      setAiRoutes([]);
      await fetchRoutes({ page: 1, limit: DEFAULT_PAGE_SIZE, search: appliedSearch || undefined });
    }
  };

  const displayedRoutes = activeTab === "favorites" ? favorites : activeTab === "ai" ? aiRoutes : routes;
  const displayedPagination =
    activeTab === "favorites" ? favoritesPagination : activeTab === "ai" ? aiPagination : pagination;
  const displayedLoading = activeTab === "ai" ? aiLoading : routesLoading;
  const displayedError = activeTab === "ai" ? aiError : errorMessage;

  return (
    <Layout className="app-layout">
      {contextHolder}
      <HomeTopBar
        onAuth={() => navigate("/auth")}
        onLogout={() => void handleLogout()}
        onCreate={() => navigate("/create")}
        onAdmin={() => navigate("/admin")}
        onMyRoutes={() => navigate("/my-routes")}
        onSecurity={() => navigate("/account")}
        isAdmin={isAdmin}
        isAuthenticated={isAuthenticated}
      />

      <Layout>
        <HomeInfoSider />
        <Content className="home-content">
          <Tabs
            activeKey={activeTab}
            onChange={(key) => void handleTabChange(key)}
            items={[
              { key: "routes", label: "Маршруты" },
              { key: "favorites", label: "Избранное" },
              ...(isAdmin ? [{ key: "ai", label: "ИИ-поиск" }] : []),
            ]}
          />

          <div className="home-toolbar">
            {activeTab === "ai" ? (
              <>
                <Input.Search
                  value={aiQuery}
                  onChange={(event) => setAiQuery(event.target.value)}
                  placeholder="Например: спокойный маршрут у воды на 2-3 часа"
                  allowClear
                  enterButton="Найти"
                  loading={aiLoading}
                  onSearch={(value) => void runAiSearch(value)}
                />
                <Alert
                  className="home-toolbar-hint"
                  type="info"
                  showIcon
                  message="ИИ-поиск ищет по смыслу среди опубликованных маршрутов."
                />
              </>
            ) : (
              <Input.Search
                value={searchInput}
                onChange={(event) => setSearchInput(event.target.value)}
                placeholder="Поиск по названию или описанию"
                allowClear
                enterButton="Найти"
                onSearch={() => void runSearch(searchInput)}
              />
            )}
          </div>

          <HomeRoutesSection
            loading={displayedLoading}
            routes={displayedRoutes}
            errorMessage={displayedError}
            pagination={displayedPagination}
            showPagination={activeTab !== "ai"}
            togglingFavoriteId={togglingFavoriteId}
            onOpenRoute={(id) => navigate(`/route/${id}`)}
            onToggleFavorite={activeTab === "ai" ? undefined : handleToggleFavorite}
            onPageChange={(page, pageSize) => void handlePageChange(page, pageSize)}
          />
        </Content>
      </Layout>

      <Footer className="app-footer">
        © 2026 RouteCraft · Туристические маршруты, карта, поиск и публикация.
      </Footer>
    </Layout>
  );
};
