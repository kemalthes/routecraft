import { Controller, type UseFormReturn } from "react-hook-form";
import { Button, Form, Input, Space } from "antd";
import type { RegisterFormValues } from "../../schema/auth.schema";

interface AuthRegisterTabProps {
  form: UseFormReturn<RegisterFormValues>;
  submitLoading: boolean;
  onSubmit: () => void;
  onResend: () => void;
}

export const AuthRegisterTab = ({
  form,
  submitLoading,
  onSubmit,
  onResend,
}: AuthRegisterTabProps) => (
  <Form layout="vertical" onFinish={onSubmit}>
    <Form.Item
      label="Username"
      validateStatus={form.formState.errors.username ? "error" : ""}
      help={form.formState.errors.username?.message}
    >
      <Controller
        name="username"
        control={form.control}
        render={({ field }) => <Input {...field} autoComplete="username" />}
      />
    </Form.Item>
    <Form.Item
      label="Email"
      validateStatus={form.formState.errors.email ? "error" : ""}
      help={form.formState.errors.email?.message}
    >
      <Controller
        name="email"
        control={form.control}
        render={({ field }) => <Input {...field} autoComplete="email" />}
      />
    </Form.Item>
    <Form.Item
      label="Пароль"
      validateStatus={form.formState.errors.password ? "error" : ""}
      help={form.formState.errors.password?.message}
    >
      <Controller
        name="password"
        control={form.control}
        render={({ field }) => <Input.Password {...field} autoComplete="new-password" />}
      />
    </Form.Item>
    <Form.Item
      label="Повтор пароля"
      validateStatus={form.formState.errors.repeatPassword ? "error" : ""}
      help={form.formState.errors.repeatPassword?.message}
    >
      <Controller
        name="repeatPassword"
        control={form.control}
        render={({ field }) => <Input.Password {...field} autoComplete="new-password" />}
      />
    </Form.Item>

    <Space orientation="vertical" size={8} style={{ width: "100%" }}>
      <Button type="primary" htmlType="submit" loading={submitLoading} block>
        Зарегистрироваться
      </Button>
      <Button onClick={onResend} block>
        Переотправить письмо подтверждения
      </Button>
    </Space>
  </Form>
);
