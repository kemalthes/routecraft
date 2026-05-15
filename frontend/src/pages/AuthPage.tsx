import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, Card, Form, Input, Layout, Space, Tabs, Typography, message } from "antd";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { Controller, useForm } from "react-hook-form";
import { authApi } from "../api/auth-api";
import { ApiClientError } from "../types/api";
import {
  forgotPasswordSchema,
  loginSchema,
  registerSchema,
  type ForgotPasswordFormValues,
  type LoginFormValues,
  type RegisterFormValues,
} from "../schema/auth.schema";
import { AuthLoginTab } from "../components/auth/AuthLoginTab";
import { AuthRegisterTab } from "../components/auth/AuthRegisterTab";
import type { AuthResponse } from "../types/auth";

const { Content } = Layout;

const extractMessage = (error: unknown) =>
  error instanceof ApiClientError ? error.message : "Операция не выполнена";

const saveAuth = (result: AuthResponse) => {
  localStorage.setItem("accessToken", result.accessToken);
  localStorage.setItem("refreshToken", result.refreshToken);
  localStorage.setItem("role", result.role);
};

export const AuthPage = () => {
  const navigate = useNavigate();
  const [messageApi, contextHolder] = message.useMessage();
  const [loginLoading, setLoginLoading] = useState(false);
  const [registerLoading, setRegisterLoading] = useState(false);
  const [registrationCodeLoading, setRegistrationCodeLoading] = useState(false);
  const [resetCodeLoading, setResetCodeLoading] = useState(false);
  const [resetLoading, setResetLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<"login" | "register" | "forgot">("login");

  const loginForm = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const registerForm = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { username: "", email: "", password: "", repeatPassword: "", code: "" },
  });

  const forgotForm = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: "", code: "", newPassword: "", repeatPassword: "" },
  });

  const submitLogin = loginForm.handleSubmit(async (values) => {
    setLoginLoading(true);
    try {
      saveAuth(await authApi.login(values));
      messageApi.success("Вы вошли в аккаунт");
      navigate("/");
    } catch (error) {
      messageApi.error(extractMessage(error));
    } finally {
      setLoginLoading(false);
    }
  });

  const sendRegistrationCode = async () => {
    const isEmailValid = await registerForm.trigger("email");
    if (!isEmailValid) {
      return;
    }
    setRegistrationCodeLoading(true);
    try {
      await authApi.sendRegistrationCode(registerForm.getValues("email").trim());
      messageApi.success("Код подтверждения отправлен");
    } catch (error) {
      messageApi.error(extractMessage(error));
    } finally {
      setRegistrationCodeLoading(false);
    }
  };

  const submitRegister = registerForm.handleSubmit(async (values) => {
    setRegisterLoading(true);
    try {
      saveAuth(await authApi.register({
        username: values.username,
        email: values.email,
        password: values.password,
        code: values.code,
      }));
      messageApi.success("Регистрация завершена");
      navigate("/");
    } catch (error) {
      messageApi.error(extractMessage(error));
    } finally {
      setRegisterLoading(false);
    }
  });

  const sendResetCode = async () => {
    const isEmailValid = await forgotForm.trigger("email");
    if (!isEmailValid) {
      return;
    }
    setResetCodeLoading(true);
    try {
      await authApi.sendPasswordResetCode(forgotForm.getValues("email").trim());
      messageApi.success("Если аккаунт существует, код отправлен");
    } catch (error) {
      messageApi.error(extractMessage(error));
    } finally {
      setResetCodeLoading(false);
    }
  };

  const submitResetPassword = forgotForm.handleSubmit(async (values) => {
    setResetLoading(true);
    try {
      await authApi.resetPassword({
        email: values.email,
        code: values.code,
        newPassword: values.newPassword,
      });
      messageApi.success("Пароль изменен. Войдите с новым паролем.");
      setActiveTab("login");
      forgotForm.reset();
    } catch (error) {
      messageApi.error(extractMessage(error));
    } finally {
      setResetLoading(false);
    }
  });

  return (
    <Layout className="auth-layout">
      {contextHolder}
      <Content className="auth-content">
        <div className="auth-shell">
          <div className="auth-back-row">
            <Link to="/">
              <Button icon={<ArrowLeftOutlined />}>На главную</Button>
            </Link>
          </div>

          <Card className="auth-card">
            <Typography.Title level={3}>Аккаунт</Typography.Title>
            {activeTab === "forgot" ? (
              <Form layout="vertical" onFinish={submitResetPassword}>
                <Form.Item
                  label="Электронная почта"
                  validateStatus={forgotForm.formState.errors.email ? "error" : ""}
                  help={forgotForm.formState.errors.email?.message}
                >
                  <Controller
                    name="email"
                    control={forgotForm.control}
                    render={({ field }) => <Input {...field} autoComplete="email" />}
                  />
                </Form.Item>
                <Form.Item
                  label="Код из письма"
                  validateStatus={forgotForm.formState.errors.code ? "error" : ""}
                  help={forgotForm.formState.errors.code?.message}
                >
                  <Controller
                    name="code"
                    control={forgotForm.control}
                    render={({ field }) => (
                      <Input {...field} maxLength={6} inputMode="numeric" autoComplete="one-time-code" />
                    )}
                  />
                </Form.Item>
                <Form.Item
                  label="Новый пароль"
                  validateStatus={forgotForm.formState.errors.newPassword ? "error" : ""}
                  help={forgotForm.formState.errors.newPassword?.message}
                >
                  <Controller
                    name="newPassword"
                    control={forgotForm.control}
                    render={({ field }) => <Input.Password {...field} autoComplete="new-password" />}
                  />
                </Form.Item>
                <Form.Item
                  label="Повторите пароль"
                  validateStatus={forgotForm.formState.errors.repeatPassword ? "error" : ""}
                  help={forgotForm.formState.errors.repeatPassword?.message}
                >
                  <Controller
                    name="repeatPassword"
                    control={forgotForm.control}
                    render={({ field }) => <Input.Password {...field} autoComplete="new-password" />}
                  />
                </Form.Item>
                <Space orientation="vertical" size={8} style={{ width: "100%" }}>
                  <Button type="primary" htmlType="submit" loading={resetLoading} block>
                    Сбросить пароль
                  </Button>
                  <Button onClick={() => void sendResetCode()} loading={resetCodeLoading} block>
                    Отправить код
                  </Button>
                  <Button type="link" onClick={() => setActiveTab("login")} block>
                    Вернуться ко входу
                  </Button>
                </Space>
              </Form>
            ) : (
              <Tabs
                activeKey={activeTab}
                onChange={(key) => setActiveTab(key as "login" | "register")}
                items={[
                  {
                    key: "login",
                    label: "Вход",
                    children: (
                      <AuthLoginTab
                        form={loginForm}
                        loading={loginLoading}
                        onSubmit={submitLogin}
                        onForgot={() => setActiveTab("forgot")}
                      />
                    ),
                  },
                  {
                    key: "register",
                    label: "Регистрация",
                    children: (
                      <AuthRegisterTab
                        form={registerForm}
                        submitLoading={registerLoading}
                        codeLoading={registrationCodeLoading}
                        onSubmit={submitRegister}
                        onSendCode={() => void sendRegistrationCode()}
                      />
                    ),
                  },
                ]}
              />
            )}
          </Card>
        </div>
      </Content>
    </Layout>
  );
};
