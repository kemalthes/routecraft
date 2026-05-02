import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, Card, Layout, Tabs, Typography, message } from "antd";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { useForm } from "react-hook-form";
import { authApi } from "../api/auth-api";
import { ApiClientError } from "../types/api";
import {
  loginSchema,
  registerSchema,
  type LoginFormValues,
  type RegisterFormValues,
} from "../schema/auth.schema";
import { AuthLoginTab } from "../components/auth/AuthLoginTab";
import { AuthRegisterTab } from "../components/auth/AuthRegisterTab";

const { Content } = Layout;

const extractMessage = (error: unknown) =>
  error instanceof ApiClientError ? error.message : "Операция завершилась с ошибкой";

export const AuthPage = () => {
  const navigate = useNavigate();
  const [messageApi, contextHolder] = message.useMessage();
  const [loginLoading, setLoginLoading] = useState(false);
  const [registerLoading, setRegisterLoading] = useState(false);

  const loginForm = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const registerForm = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { username: "", email: "", password: "", repeatPassword: "" },
  });

  const submitLogin = loginForm.handleSubmit(async (values) => {
    setLoginLoading(true);
    try {
      const result = await authApi.login(values);
      localStorage.setItem("accessToken", result.accessToken);
      localStorage.setItem("refreshToken", result.refreshToken);
      localStorage.setItem("role", result.role);
      messageApi.success("Вход выполнен");
      navigate("/");
    } catch (error) {
      messageApi.error(extractMessage(error));
    } finally {
      setLoginLoading(false);
    }
  });

  const submitRegister = registerForm.handleSubmit(async (values) => {
    setRegisterLoading(true);
    try {
      const exists = await authApi.checkEmailExists(values.email.trim());
      if (exists) {
        registerForm.setError("email", {
          type: "manual",
          message: "Этот email уже зарегистрирован",
        });
        return;
      }

      const result = await authApi.register({
        username: values.username,
        email: values.email,
        password: values.password,
      });
      localStorage.setItem("accessToken", result.accessToken);
      localStorage.setItem("refreshToken", result.refreshToken);
      localStorage.setItem("role", result.role);
      messageApi.success("Регистрация выполнена");
      navigate("/");
    } catch (error) {
      messageApi.error(extractMessage(error));
    } finally {
      setRegisterLoading(false);
    }
  });

  const handleResendFromRegistration = async () => {
    const email = registerForm.getValues("email").trim();
    if (!email) {
      registerForm.setError("email", {
        type: "manual",
        message: "Укажите email для переотправки письма",
      });
      return;
    }

    try {
      const exists = await authApi.checkEmailExists(email);
      if (!exists) {
        registerForm.setError("email", {
          type: "manual",
          message: "Email не найден. Проверьте адрес или зарегистрируйтесь.",
        });
        return;
      }
      messageApi.info("Endpoint переотправки еще не реализован на бэкенде.");
    } catch (error) {
      messageApi.error(extractMessage(error));
    }
  };

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
            <Typography.Title level={3}>Авторизация</Typography.Title>
            <Tabs
              items={[
                {
                  key: "login",
                  label: "Вход",
                  children: <AuthLoginTab form={loginForm} loading={loginLoading} onSubmit={submitLogin} />,
                },
                {
                  key: "register",
                  label: "Регистрация",
                  children: (
                    <AuthRegisterTab
                      form={registerForm}
                      submitLoading={registerLoading}
                      onSubmit={submitRegister}
                      onResend={() => void handleResendFromRegistration()}
                    />
                  ),
                },
              ]}
            />
          </Card>
        </div>
      </Content>
    </Layout>
  );
};
