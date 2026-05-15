import { Controller, type UseFormReturn } from "react-hook-form";
import { Button, Form, Input, Space } from "antd";
import type { RegisterFormValues } from "../../schema/auth.schema";

interface AuthRegisterTabProps {
  form: UseFormReturn<RegisterFormValues>;
  submitLoading: boolean;
  codeLoading: boolean;
  onSubmit: () => void;
  onSendCode: () => void;
}

export const AuthRegisterTab = ({
  form,
  submitLoading,
  codeLoading,
  onSubmit,
  onSendCode,
}: AuthRegisterTabProps) => (
  <Form layout="vertical" onFinish={onSubmit}>
    <Form.Item
      label="Имя пользователя"
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
      label="Электронная почта"
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
    <Form.Item
      label="Код из письма"
      validateStatus={form.formState.errors.code ? "error" : ""}
      help={form.formState.errors.code?.message}
    >
      <Controller
        name="code"
        control={form.control}
        render={({ field }) => <Input {...field} maxLength={6} inputMode="numeric" autoComplete="one-time-code" />}
      />
    </Form.Item>

    <Space orientation="vertical" size={8} style={{ width: "100%" }}>
      <Button
        type="primary"
        htmlType="submit"
        loading={submitLoading}
        disabled={!form.watch("code")?.trim()}
        block
      >
        Зарегистрироваться
      </Button>
      <Button onClick={onSendCode} loading={codeLoading} block>
        Отправить код
      </Button>
    </Space>
  </Form>
);
