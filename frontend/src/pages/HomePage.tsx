import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Input, Layout } from "antd";
import { useRoutesStore } from "../store/routes-store";
import { HomeTopBar } from "../components/home/HomeTopBar";
import { HomeInfoSider } from "../components/home/HomeInfoSider";
import { HomeRoutesSection } from "../components/home/HomeRoutesSection";
import { HomeAiSearchModal } from "../components/home/HomeAiSearchModal";

const { Content, Footer } = Layout;
const DEFAULT_PAGE_SIZE = 12;

export const HomePage = () => {
  const navigate = useNavigate();
  const routes = useRoutesStore((state) => state.routes);
  const pagination = useRoutesStore((state) => state.pagination);
  const routesLoading = useRoutesStore((state) => state.routesLoading);
  const errorMessage = useRoutesStore((state) => state.errorMessage);
  const fetchRoutes = useRoutesStore((state) => state.fetchRoutes);

  const [searchInput, setSearchInput] = useState("");
  const [appliedSearch, setAppliedSearch] = useState("");
  const [aiModalOpen, setAiModalOpen] = useState(false);
  const [aiQuery, setAiQuery] = useState("");

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
    await fetchRoutes({
      page,
      limit: pageSize,
      search: appliedSearch || undefined,
    });
  };

  return (
    <Layout className="app-layout">
      <HomeTopBar
        onAuth={() => navigate("/auth")}
        onCreate={() => navigate("/create")}
        onOpenAiSearch={() => setAiModalOpen(true)}
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

          <HomeRoutesSection
            loading={routesLoading}
            routes={routes}
            errorMessage={errorMessage}
            pagination={pagination}
            onOpenRoute={(id) => navigate(`/route/${id}`)}
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
