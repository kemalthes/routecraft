import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Input, Layout, Tabs, message } from "antd";
import { useRoutesStore } from "../store/routes-store";
import { HomeTopBar } from "../components/home/HomeTopBar";
import { HomeInfoSider } from "../components/home/HomeInfoSider";
import { HomeRoutesSection } from "../components/home/HomeRoutesSection";
import { HomeAiSearchModal } from "../components/home/HomeAiSearchModal";
import { authApi } from "../api/auth-api";

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
  const [aiModalOpen, setAiModalOpen] = useState(false);
  const [aiQuery, setAiQuery] = useState("");
  const [messageApi, contextHolder] = message.useMessage();

  const role = localStorage.getItem("role");
  const isAuthenticated = Boolean(localStorage.getItem("accessToken"));
  const isAdmin = role === "ROLE_ADMIN" || role === "ADMIN";

  useEffect(() => {
    void fetchRoutes({ page: 1, limit: DEFAULT_PAGE_SIZE });
  }, [fetchRoutes]);

  const runSearch = async (query: string) => {
    const normalized = query.trim();
    setAppliedSearch(normalized);
    await fetchRoutes({
      page: 1,
      limit: pagination.itemsPerPage || DEFAULT_PAGE_SIZE,
      search: normalized || undefined,
    });
  };

  const handlePageChange = async (page: number, pageSize: number) => {
    if (activeTab === "favorites") {
      await fetchFavorites({ page, limit: pageSize });
      return;
    }

    await fetchRoutes({ page, limit: pageSize, search: appliedSearch || undefined });
  };

  const handleTabChange = async (key: string) => {
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
      await fetchRoutes({ page: 1, limit: DEFAULT_PAGE_SIZE, search: appliedSearch || undefined });
    }
  };

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
        onOpenAiSearch={() => setAiModalOpen(true)}
        isAdmin={isAdmin}
        isAuthenticated={isAuthenticated}
      />

      <Layout>
        <HomeInfoSider />
        <Content className="home-content">
          <div className="home-toolbar">
            <Input.Search
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Поиск по названию или описанию"
              allowClear
              enterButton="Найти"
              onSearch={() => void runSearch(searchInput)}
            />
          </div>

          <Tabs
            activeKey={activeTab}
            onChange={(key) => void handleTabChange(key)}
            items={[
              { key: "routes", label: "Маршруты" },
              { key: "favorites", label: "Избранное" },
            ]}
          />

          <HomeRoutesSection
            loading={routesLoading}
            routes={activeTab === "favorites" ? favorites : routes}
            errorMessage={errorMessage}
            pagination={activeTab === "favorites" ? favoritesPagination : pagination}
            togglingFavoriteId={togglingFavoriteId}
            onOpenRoute={(id) => navigate(`/route/${id}`)}
            onToggleFavorite={handleToggleFavorite}
            onPageChange={(page, pageSize) => void handlePageChange(page, pageSize)}
          />
        </Content>
      </Layout>

      <Footer className="app-footer">
        © 2026 RouteCraft · Туристические маршруты, карта, поиск и публикация.
      </Footer>

      <HomeAiSearchModal
        open={aiModalOpen}
        query={aiQuery}
        onChangeQuery={setAiQuery}
        onApply={() => {
          setSearchInput(aiQuery);
          void runSearch(aiQuery);
          setAiModalOpen(false);
        }}
        onClose={() => setAiModalOpen(false)}
      />
    </Layout>
  );
};
