import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Button, Card, Form, Input, Layout, Pagination, Space, Spin, Tag, Typography, message } from "antd";
import { ArrowLeftOutlined, DeleteOutlined, SaveOutlined } from "@ant-design/icons";
import { ApiClientError } from "../types/api";
import { useRoutesStore } from "../store/routes-store";
import type { RoutePreview } from "../types/routes";

const { Content } = Layout;

type DraftValues = Record<string, { title: string; description: string }>;

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
  const [busyRouteId, setBusyRouteId] = useState<string | null>(null);

  useEffect(() => {
    void fetchMyRoutes({ page: 1, limit: 10 });
  }, [fetchMyRoutes]);

  useEffect(() => {
    setDrafts(toDraftValues(myRoutes));
  }, [myRoutes]);

  const routes = useMemo(
    () => myRoutes.filter((route) => route.status !== "PUBLISHED"),
    [myRoutes],
  );

  const handleConflict = async (error: unknown) => {
    if (error instanceof ApiClientError && error.status === 409) {
      messageApi.warning("Route was changed by another request. Fresh data loaded.");
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
    setBusyRouteId(route.id);
    try {
      await updateRoute({
        uuid: route.id,
        version: route.version,
        title: draft.title.trim(),
        description: draft.description.trim(),
      });
      messageApi.success("Route updated");
    } catch (error) {
      if (!(await handleConflict(error))) {
        messageApi.error(error instanceof ApiClientError ? error.message : "Route update failed");
      }
    } finally {
      setBusyRouteId(null);
    }
  };

  const handleDelete = async (route: RoutePreview) => {
    setBusyRouteId(route.id);
    try {
      await deleteRoute(route.id, route.version);
      messageApi.success("Route deleted");
    } catch (error) {
      if (!(await handleConflict(error))) {
        messageApi.error(error instanceof ApiClientError ? error.message : "Route delete failed");
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
          <Link to="/">
            <Button icon={<ArrowLeftOutlined />}>Back</Button>
          </Link>
          <Typography.Title level={3} style={{ margin: 0 }}>
            My unpublished routes
          </Typography.Title>
        </div>

        {loading ? (
          <div className="content-loader">
            <Spin size="large" />
          </div>
        ) : errorMessage ? (
          <Typography.Text type="danger">{errorMessage}</Typography.Text>
        ) : (
          <Space orientation="vertical" size={16} style={{ width: "100%" }}>
            {routes.length === 0 && <Typography.Text>No unpublished routes</Typography.Text>}
            {routes.map((route) => (
              <Card key={route.id} className="route-manage-card">
                <div className="route-manage-grid">
                  <img src={route.imageUrl} alt={route.title} className="route-manage-image" />
                  <Form layout="vertical" requiredMark={false}>
                    <Space style={{ marginBottom: 8 }}>
                      <Tag>{route.status ?? "DRAFT"}</Tag>
                      <Typography.Text type="secondary">Version {route.version ?? 0}</Typography.Text>
                    </Space>
                    <Form.Item label="Title">
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
                    <Form.Item label="Description">
                      <Input.TextArea
                        rows={4}
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
                    <Space wrap>
                      <Button
                        icon={<SaveOutlined />}
                        loading={busyRouteId === route.id}
                        type="primary"
                        onClick={() => void handleSave(route)}
                      >
                        Save
                      </Button>
                      <Button
                        danger
                        icon={<DeleteOutlined />}
                        loading={busyRouteId === route.id}
                        onClick={() => void handleDelete(route)}
                      >
                        Delete
                      </Button>
                    </Space>
                  </Form>
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
