import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, Card, Descriptions, Form, Input, Layout, Space, Spin, Typography, message } from "antd";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { Controller, useForm } from "react-hook-form";
import { authApi } from "../api/auth-api";
import { usersApi } from "../api/users-api";
import { ApiClientError } from "../types/api";
import { changePasswordSchema, type ChangePasswordFormValues } from "../schema/auth.schema";
import type { CurrentUserResponse } from "../types/user";

const { Content } = Layout;

const extractMessage = (error: unknown) =>
  error instanceof ApiClientError ? error.message : "Операция не выполнена";

export const SecurityPage = () => {
  const navigate = useNavigate();
  const [messageApi, contextHolder] = message.useMessage();
  const [currentUser, setCurrentUser] = useState<CurrentUserResponse | null>(null);
  const [profileLoading, setProfileLoading] = useState(true);
  const [profileError, setProfileError] = useState<string | null>(null);
  const form = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: "", newPassword: "", repeatPassword: "" },
  });

  useEffect(() => {
    let cancelled = false;
    setProfileLoading(true);
    usersApi.getCurrentUser()
      .then((user) => {
        if (!cancelled) {
          setCurrentUser(user);
          setProfileError(null);
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setProfileError(extractMessage(error));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setProfileLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const submit = form.handleSubmit(async (values) => {
    try {
      await authApi.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("role");
      setCurrentUser(null);
      messageApi.success("Пароль изменен. Войдите снова.");
      navigate("/auth");
    } catch (error) {
      messageApi.error(extractMessage(error));
    }
  });

  return (
    <Layout className="details-layout">
      {contextHolder}
      <Content className="details-content">
        <div className="details-header">
          <Link to="/">
            <Button icon={<ArrowLeftOutlined />}>Назад</Button>
          </Link>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Аккаунт
          </Typography.Title>
        </div>

        <div className="account-grid">
          <Card title="Профиль" className="auth-card">
            {profileLoading ? (
              <div className="content-loader account-loader">
                <Spin />
              </div>
            ) : profileError ? (
              <Typography.Text type="danger">{profileError}</Typography.Text>
            ) : currentUser ? (
              <Descriptions bordered column={1} size="small">
                <Descriptions.Item label="Имя пользователя">{currentUser.username}</Descriptions.Item>
                <Descriptions.Item label="Электронная почта">{currentUser.email}</Descriptions.Item>
                <Descriptions.Item label="Роль">{currentUser.role}</Descriptions.Item>
                <Descriptions.Item label="Идентификатор пользователя">{currentUser.id}</Descriptions.Item>
                {currentUser.createdAt && (
                  <Descriptions.Item label="Создан">
                    {new Date(currentUser.createdAt).toLocaleString()}
                  </Descriptions.Item>
                )}
              </Descriptions>
            ) : (
              <Typography.Text type="secondary">Профиль недоступен</Typography.Text>
            )}
          </Card>

          <Card title="Пароль" className="auth-card">
            <Form layout="vertical" onFinish={submit}>
              <Form.Item
                label="Текущий пароль"
                validateStatus={form.formState.errors.currentPassword ? "error" : ""}
                help={form.formState.errors.currentPassword?.message}
              >
                <Controller
                  name="currentPassword"
                  control={form.control}
                  render={({ field }) => <Input.Password {...field} autoComplete="current-password" />}
                />
              </Form.Item>
              <Form.Item
                label="Новый пароль"
                validateStatus={form.formState.errors.newPassword ? "error" : ""}
                help={form.formState.errors.newPassword?.message}
              >
                <Controller
                  name="newPassword"
                  control={form.control}
                  render={({ field }) => <Input.Password {...field} autoComplete="new-password" />}
                />
              </Form.Item>
              <Form.Item
                label="Повторите пароль"
                validateStatus={form.formState.errors.repeatPassword ? "error" : ""}
                help={form.formState.errors.repeatPassword?.message}
              >
                <Controller
                  name="repeatPassword"
                  control={form.control}
                  render={({ field }) => <Input.Password {...field} autoComplete="new-password" />}
                />
              </Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={form.formState.isSubmitting}>
                  Изменить пароль
                </Button>
              </Space>
            </Form>
          </Card>
        </div>
      </Content>
    </Layout>
  );
};
