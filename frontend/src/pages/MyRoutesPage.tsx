import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Button, Card, Form, Input, Layout, Pagination, Space, Spin, Tabs, Tag, Typography, message } from "antd";
import { ArrowLeftOutlined, DeleteOutlined, PlusOutlined, SaveOutlined } from "@ant-design/icons";
import { routesApi } from "../api/routes-api";
import { ApiClientError } from "../types/api";
import { RouteReadonlyMap } from "../components/routes/RouteReadonlyMap";
import { useRoutesStore } from "../store/routes-store";
import type { RouteFull, RoutePreview } from "../types/routes";

const { Content } = Layout;

type DraftValues = Record<string, { title: string; description: string }>;
type RouteTab = "pending" | "published";

const statusLabel = (status?: string) => {
  if (status === "PUBLISHED") {
    return "Опубликован";
  }
  if (status === "PENDING") {
    return "На проверке";
  }
  return "Черновик";
};

const validateRouteDraft = (title: string, description: string) => {
  if (title.length < 5) {
    return "Название должно содержать минимум 5 символов";
  }
  if (title.length > 100) {
    return "Название должно содержать максимум 100 символов";
  }
  if (description.length > 2000) {
    return "Описание должно содержать максимум 2000 символов";
  }
  return null;
};

const toDraftValues = (routes: RoutePreview[]): DraftValues =>
  Object.fromEntries(
    routes.map((route) => [
      route.id,
      {
        title: route.title,
        description: route.description ?? "",
      },
    ]),
  );

export const MyRoutesPage = () => {
  const [messageApi, contextHolder] = message.useMessage();
  const myRoutes = useRoutesStore((state) => state.myRoutes);
  const pagination = useRoutesStore((state) => state.myRoutesPagination);
  const loading = useRoutesStore((state) => state.routesLoading);
  const errorMessage = useRoutesStore((state) => state.errorMessage);
  const fetchMyRoutes = useRoutesStore((state) => state.fetchMyRoutes);
  const updateRoute = useRoutesStore((state) => state.updateRoute);
  const deleteRoute = useRoutesStore((state) => state.deleteRoute);
  const [drafts, setDrafts] = useState<DraftValues>({});
  const [routeDetailsById, setRouteDetailsById] = useState<Record<string, RouteFull>>({});
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [busyRouteId, setBusyRouteId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<RouteTab>("pending");

  useEffect(() => {
    void fetchMyRoutes({ page: 1, limit: 10 });
  }, [fetchMyRoutes]);

  useEffect(() => {
    setDrafts(toDraftValues(myRoutes));
  }, [myRoutes]);

  const pendingRoutes = useMemo(
    () => myRoutes.filter((route) => route.status === "PENDING"),
    [myRoutes],
  );

  const publishedRoutes = useMemo(
    () => myRoutes.filter((route) => route.status === "PUBLISHED"),
    [myRoutes],
  );

  const routes = activeTab === "published" ? publishedRoutes : pendingRoutes;

  useEffect(() => {
    if (routes.length === 0) {
      setRouteDetailsById({});
      setDetailsLoading(false);
      return;
    }

    let cancelled = false;
    setDetailsLoading(true);
    Promise.all(
      routes.map((route) =>
        (route.status === "PUBLISHED" ? routesApi.getRouteById(route.id) : routesApi.getMyRouteById(route.id))
          .catch(() => null),
      ),
    )
      .then((routeDetails) => {
        if (cancelled) {
          return;
        }
        setRouteDetailsById(
          Object.fromEntries(
            routeDetails
              .filter((route): route is RouteFull => route !== null)
              .map((route) => [route.id, route]),
          ),
        );
      })
      .finally(() => {
        if (!cancelled) {
          setDetailsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [routes]);

  const handleConflict = async (error: unknown) => {
    if (error instanceof ApiClientError && error.status === 409) {
      messageApi.warning("Маршрут изменился в другом запросе. Загружены свежие данные.");
      await fetchMyRoutes({ page: pagination.currentPage, limit: pagination.itemsPerPage });
      return true;
    }
    return false;
  };

  const handleSave = async (route: RoutePreview) => {
    const draft = drafts[route.id];
    if (!draft) {
      return;
    }
    const title = draft.title.trim();
    const description = draft.description.trim();
    const validationMessage = validateRouteDraft(title, description);
    if (validationMessage) {
      messageApi.warning(validationMessage);
      return;
    }
    setBusyRouteId(route.id);
    try {
      await updateRoute({
        uuid: route.id,
        version: route.version,
        title,
        description,
      });
      messageApi.success("Маршрут обновлен");
    } catch (error) {
      if (!(await handleConflict(error))) {
        const validationError = error instanceof ApiClientError
          ? error.validationErrors.map((fieldError) => `${fieldError.field}: ${fieldError.message}`).join("; ")
          : "";
        messageApi.error(validationError || (error instanceof ApiClientError ? error.message : "Не удалось обновить маршрут"));
      }
    } finally {
      setBusyRouteId(null);
    }
  };

  const handleDelete = async (route: RoutePreview) => {
    setBusyRouteId(route.id);
    try {
      await deleteRoute(route.id, route.version);
      messageApi.success("Маршрут удален");
    } catch (error) {
      if (!(await handleConflict(error))) {
        messageApi.error(error instanceof ApiClientError ? error.message : "Не удалось удалить маршрут");
      }
    } finally {
      setBusyRouteId(null);
    }
  };

  return (
    <Layout className="details-layout">
      {contextHolder}
      <Content className="details-content">
        <div className="details-header">
          <Space>
            <Link to="/">
              <Button icon={<ArrowLeftOutlined />}>Назад</Button>
            </Link>
            <div>
              <Typography.Title level={3} style={{ margin: 0 }}>
                Мои маршруты
              </Typography.Title>
              <Typography.Text type="secondary">Проверяйте статус, карту и данные своих маршрутов.</Typography.Text>
            </div>
          </Space>
          <Link to="/create">
            <Button type="primary" icon={<PlusOutlined />}>
              Создать маршрут
            </Button>
          </Link>
        </div>

        {loading ? (
          <div className="content-loader">
            <Spin size="large" />
          </div>
        ) : errorMessage ? (
          <Typography.Text type="danger">{errorMessage}</Typography.Text>
        ) : (
          <Space orientation="vertical" size={16} style={{ width: "100%" }}>
            <Tabs
              activeKey={activeTab}
              onChange={(key) => setActiveTab(key as RouteTab)}
              items={[
                { key: "pending", label: `На проверке (${pendingRoutes.length})` },
                { key: "published", label: `Опубликованные (${publishedRoutes.length})` },
              ]}
            />
            {routes.length === 0 && <Typography.Text>Маршрутов в этом разделе нет</Typography.Text>}
            {routes.map((route) => (
              <Card key={route.id} className="route-manage-card">
                <div className="route-edit-layout">
                  <div className="route-edit-map-panel">
                    <div className="route-edit-map">
                      {routeDetailsById[route.id]?.locations?.length ? (
                        <RouteReadonlyMap
                          locations={routeDetailsById[route.id].locations}
                          geometry={routeDetailsById[route.id].geometry}
                        />
                      ) : detailsLoading ? (
                        <Spin />
                      ) : (
                        <Typography.Text type="secondary">Карта недоступна</Typography.Text>
                      )}
                    </div>
                    <div className="route-edit-preview">
                      <img src={route.imageUrl} alt={route.title} className="route-edit-image" />
                      <div className="route-edit-summary">
                        <Typography.Text strong>{route.title}</Typography.Text>
                        <Typography.Text type="secondary">
                          {route.distance.toFixed(1)} км · {route.durationMinutes} мин
                        </Typography.Text>
                      </div>
                    </div>
                  </div>
                  <div className="route-edit-form-panel">
                    <div className="route-edit-status-row">
                      <Tag>{statusLabel(route.status)}</Tag>
                      <Typography.Text type="secondary">Версия {route.version ?? 0}</Typography.Text>
                    </div>
                    {route.status === "PUBLISHED" ? (
                      <Space orientation="vertical" size={12} style={{ width: "100%" }}>
                        <Typography.Title level={4} style={{ margin: 0 }}>
                          {route.title}
                        </Typography.Title>
                        <Typography.Paragraph>{route.description || "Описание не заполнено"}</Typography.Paragraph>
                        <Typography.Text type="secondary">
                          Опубликованные маршруты доступны пользователям и не редактируются из этого раздела.
                        </Typography.Text>
                        <Link to={`/route/${route.id}`}>
                          <Button type="primary">Открыть маршрут</Button>
                        </Link>
                      </Space>
                    ) : (
                      <Form layout="vertical" requiredMark={false}>
                        <Form.Item label="Название">
                          <Input
                            value={drafts[route.id]?.title ?? route.title}
                            onChange={(event) =>
                              setDrafts((current) => ({
                                ...current,
                                [route.id]: {
                                  title: event.target.value,
                                  description: current[route.id]?.description ?? route.description ?? "",
                                },
                              }))
                            }
                          />
                        </Form.Item>
                        <Form.Item label="Описание">
                          <Input.TextArea
                            rows={5}
                            value={drafts[route.id]?.description ?? route.description ?? ""}
                            onChange={(event) =>
                              setDrafts((current) => ({
                                ...current,
                                [route.id]: {
                                  title: current[route.id]?.title ?? route.title,
                                  description: event.target.value,
                                },
                              }))
                            }
                          />
                        </Form.Item>
                        <Space wrap className="route-edit-actions">
                          <Button
                            icon={<SaveOutlined />}
                            loading={busyRouteId === route.id}
                            type="primary"
                            onClick={() => void handleSave(route)}
                          >
                            Сохранить
                          </Button>
                          <Button
                            danger
                            icon={<DeleteOutlined />}
                            loading={busyRouteId === route.id}
                            onClick={() => void handleDelete(route)}
                          >
                            Удалить
                          </Button>
                        </Space>
                      </Form>
                    )}
                  </div>
                </div>
              </Card>
            ))}
            <Pagination
              current={pagination.currentPage}
              pageSize={pagination.itemsPerPage}
              total={pagination.totalItems}
              onChange={(page, limit) => void fetchMyRoutes({ page, limit })}
            />
          </Space>
        )}
      </Content>
    </Layout>
  );
};
