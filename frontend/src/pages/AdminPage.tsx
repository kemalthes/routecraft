import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  Layout,
  Pagination,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
  message,
} from "antd";
import { ArrowLeftOutlined, CheckOutlined, DeleteOutlined } from "@ant-design/icons";
import { adminApi } from "../api/admin-api";
import { ApiClientError } from "../types/api";
import type { PaginationMeta, RoutePreview } from "../types/routes";
import type { CurrentUserResponse } from "../types/user";

const { Content } = Layout;

const defaultMeta: PaginationMeta = {
  totalItems: 0,
  totalPages: 0,
  currentPage: 1,
  itemsPerPage: 10,
};

type DraftValues = Record<string, { title: string; description: string }>;

export const AdminPage = () => {
  const navigate = useNavigate();
  const [messageApi, contextHolder] = message.useMessage();
  const [users, setUsers] = useState<CurrentUserResponse[]>([]);
  const [routes, setRoutes] = useState<RoutePreview[]>([]);
  const [usersMeta, setUsersMeta] = useState(defaultMeta);
  const [routesMeta, setRoutesMeta] = useState(defaultMeta);
  const [status, setStatus] = useState<string>("PENDING");
  const [loading, setLoading] = useState(false);
  const [busyRouteId, setBusyRouteId] = useState<string | null>(null);
  const [drafts, setDrafts] = useState<DraftValues>({});

  const ensureAdmin = () => {
    const role = localStorage.getItem("role");
    if (role !== "ROLE_ADMIN" && role !== "ADMIN") {
      navigate("/");
      return false;
    }
    return true;
  };

  const loadData = async (params?: { usersPage?: number; routesPage?: number; routeStatus?: string }) => {
    if (!ensureAdmin()) {
      return;
    }
    setLoading(true);
    try {
      const [usersResponse, routesResponse] = await Promise.all([
        adminApi.getUsers({ page: params?.usersPage ?? usersMeta.currentPage, limit: usersMeta.itemsPerPage }),
        adminApi.getRoutes({
          page: params?.routesPage ?? routesMeta.currentPage,
          limit: routesMeta.itemsPerPage,
          status: params?.routeStatus ?? status,
        }),
      ]);
      setUsers(usersResponse.items ?? []);
      setUsersMeta(usersResponse.meta ?? defaultMeta);
      setRoutes(routesResponse.items ?? []);
      setRoutesMeta(routesResponse.meta ?? defaultMeta);
      setDrafts(
        Object.fromEntries(
          (routesResponse.items ?? []).map((route) => [
            route.id,
            { title: route.title, description: route.description ?? "" },
          ]),
        ),
      );
    } catch (error) {
      messageApi.error(error instanceof ApiClientError ? error.message : "Admin data loading failed");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData({ usersPage: 1, routesPage: 1, routeStatus: status });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleConflict = async (error: unknown) => {
    if (error instanceof ApiClientError && error.status === 409) {
      messageApi.warning("Route was changed by another request. Fresh data loaded.");
      await loadData();
      return true;
    }
    return false;
  };

  const approveRoute = async (route: RoutePreview) => {
    const draft = drafts[route.id];
    if (!draft) {
      return;
    }
    setBusyRouteId(route.id);
    try {
      await adminApi.approveRoute({
        uuid: route.id,
        version: route.version,
        title: draft.title.trim(),
        description: draft.description.trim(),
      });
      messageApi.success("Route approved");
      await loadData();
    } catch (error) {
      if (!(await handleConflict(error))) {
        messageApi.error(error instanceof ApiClientError ? error.message : "Route approval failed");
      }
    } finally {
      setBusyRouteId(null);
    }
  };

  const deleteRoute = async (route: RoutePreview) => {
    setBusyRouteId(route.id);
    try {
      await adminApi.deleteRoute({ uuid: route.id, version: route.version });
      messageApi.success("Route deleted");
      await loadData();
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
            Admin panel
          </Typography.Title>
        </div>

        {loading ? (
          <div className="content-loader">
            <Spin size="large" />
          </div>
        ) : (
          <div className="admin-grid">
            <Card title="Users">
              <Table
                dataSource={users}
                pagination={false}
                rowKey="id"
                size="small"
                columns={[
                  { title: "Name", dataIndex: "username" },
                  { title: "Email", dataIndex: "email" },
                  { title: "Role", dataIndex: "role" },
                  { title: "Created", dataIndex: "createdAt" },
                ]}
              />
              <Pagination
                current={usersMeta.currentPage}
                pageSize={usersMeta.itemsPerPage}
                total={usersMeta.totalItems}
                onChange={(page) => void loadData({ usersPage: page })}
                style={{ marginTop: 16 }}
              />
            </Card>

            <Card
              title="Route moderation"
              extra={
                <Select
                  value={status}
                  style={{ width: 140 }}
                  options={[
                    { value: "PENDING", label: "Pending" },
                    { value: "DRAFT", label: "Draft" },
                    { value: "PUBLISHED", label: "Published" },
                  ]}
                  onChange={(value) => {
                    setStatus(value);
                    void loadData({ routesPage: 1, routeStatus: value });
                  }}
                />
              }
            >
              <Space orientation="vertical" size={16} style={{ width: "100%" }}>
                {routes.length === 0 && <Typography.Text>No routes</Typography.Text>}
                {routes.map((route) => (
                  <Card key={route.id} className="route-manage-card">
                    <div className="route-manage-grid">
                      <img src={route.imageUrl} alt={route.title} className="route-manage-image" />
                      <Form layout="vertical" requiredMark={false}>
                        <Space style={{ marginBottom: 8 }} wrap>
                          <Tag>{route.status}</Tag>
                          <Typography.Text type="secondary">Version {route.version ?? 0}</Typography.Text>
                        </Space>
                        <Descriptions size="small" column={1} style={{ marginBottom: 12 }}>
                          <Descriptions.Item label="Author">{route.authorName}</Descriptions.Item>
                          <Descriptions.Item label="Distance">{route.distance?.toFixed(1)} km</Descriptions.Item>
                        </Descriptions>
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
                            icon={<CheckOutlined />}
                            loading={busyRouteId === route.id}
                            type="primary"
                            onClick={() => void approveRoute(route)}
                          >
                            Approve
                          </Button>
                          <Button
                            danger
                            icon={<DeleteOutlined />}
                            loading={busyRouteId === route.id}
                            onClick={() => void deleteRoute(route)}
                          >
                            Delete
                          </Button>
                        </Space>
                      </Form>
                    </div>
                  </Card>
                ))}
                <Pagination
                  current={routesMeta.currentPage}
                  pageSize={routesMeta.itemsPerPage}
                  total={routesMeta.totalItems}
                  onChange={(page) => void loadData({ routesPage: page })}
                />
              </Space>
            </Card>
          </div>
        )}
      </Content>
    </Layout>
  );
};
