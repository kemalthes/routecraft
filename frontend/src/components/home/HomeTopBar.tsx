import { Button, Layout, Space, Typography } from "antd";
import {
  LoginOutlined,
  LogoutOutlined,
  PlusOutlined,
  RobotOutlined,
  SettingOutlined,
  UserOutlined,
} from "@ant-design/icons";

const { Header } = Layout;

interface HomeTopBarProps {
  onAuth: () => void;
  onLogout: () => void;
  onCreate: () => void;
  onAdmin?: () => void;
  onMyRoutes?: () => void;
  onSecurity?: () => void;
  onOpenAiSearch: () => void;
  isAdmin?: boolean;
  isAuthenticated?: boolean;
}

export const HomeTopBar = ({
  onAuth,
  onLogout,
  onCreate,
  onAdmin,
  onMyRoutes,
  onSecurity,
  onOpenAiSearch,
  isAdmin,
  isAuthenticated,
}: HomeTopBarProps) => (
  <Header className="app-header">
    <div className="app-header-main">
      <Typography.Title level={4} className="brand-title">
        RouteCraft
      </Typography.Title>
      <Typography.Text type="secondary" className="brand-subtitle">
        Маршруты, карты и модерация
      </Typography.Text>
    </div>
    <div className="header-actions">
      <Space wrap size={8}>
        {isAdmin && (
          <Button icon={<RobotOutlined />} onClick={onOpenAiSearch}>
            ИИ-поиск
          </Button>
        )}
        <Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>
          Создать
        </Button>
      </Space>
      <Space wrap size={8}>
        {isAuthenticated && (
          <Button icon={<UserOutlined />} onClick={onMyRoutes}>
            Мои маршруты
          </Button>
        )}
        {isAuthenticated && (
          <Button icon={<UserOutlined />} onClick={onSecurity}>
            Аккаунт
          </Button>
        )}
        {isAdmin && (
          <Button icon={<SettingOutlined />} onClick={onAdmin}>
            Админка
          </Button>
        )}
        {isAuthenticated ? (
          <Button icon={<LogoutOutlined />} onClick={onLogout}>
            Выйти
          </Button>
        ) : (
          <Button icon={<LoginOutlined />} onClick={onAuth}>
            Войти
          </Button>
        )}
      </Space>
    </div>
  </Header>
);
