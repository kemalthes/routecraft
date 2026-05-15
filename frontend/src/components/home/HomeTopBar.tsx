import { Button, Layout, Space, Typography } from "antd";
import { RobotOutlined, SettingOutlined, UserOutlined } from "@ant-design/icons";

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
    <Typography.Title level={4} className="brand-title">
      RouteCraft
    </Typography.Title>
    <Space wrap>
      {isAuthenticated ? (
        <Button onClick={onLogout}>Logout</Button>
      ) : (
        <Button onClick={onAuth}>Sign in</Button>
      )}
      {isAuthenticated && (
        <Button icon={<UserOutlined />} onClick={onMyRoutes}>
          My routes
        </Button>
      )}
      {isAuthenticated && (
        <Button onClick={onSecurity}>
          Security
        </Button>
      )}
      {isAdmin && (
        <Button icon={<SettingOutlined />} onClick={onAdmin}>
          Admin
        </Button>
      )}
      <Button type="primary" onClick={onCreate}>
        Create
      </Button>
      <Button icon={<RobotOutlined />} onClick={onOpenAiSearch}>
        AI search
      </Button>
    </Space>
  </Header>
);
