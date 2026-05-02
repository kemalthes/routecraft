import { Controller, type UseFormReturn } from "react-hook-form";
import { Button, Form, Input } from "antd";
import type { LoginFormValues } from "../../schema/auth.schema";

interface AuthLoginTabProps {
  form: UseFormReturn<LoginFormValues>;
  loading: boolean;
  onSubmit: () => void;
}

export const AuthLoginTab = ({ form, loading, onSubmit }: AuthLoginTabProps) => (
  <Form layout="vertical" onFinish={onSubmit}>
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
        render={({ field }) => <Input.Password {...field} autoComplete="current-password" />}
      />
    </Form.Item>
    <Button type="primary" htmlType="submit" loading={loading} block>
      Войти
    </Button>
  </Form>
);
