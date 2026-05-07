import { Button, Layout, Space, Typography } from "antd";
import { RobotOutlined, SettingOutlined, UserOutlined } from "@ant-design/icons";

const { Header } = Layout;

interface HomeTopBarProps {
  onAuth: () => void;
  onCreate: () => void;
  onAdmin?: () => void;
  onMyRoutes?: () => void;
  onOpenAiSearch: () => void;
  isAdmin?: boolean;
  isAuthenticated?: boolean;
}

export const HomeTopBar = ({
  onAuth,
  onCreate,
  onAdmin,
  onMyRoutes,
  onOpenAiSearch,
  isAdmin,
  isAuthenticated,
}: HomeTopBarProps) => (
  <Header className="app-header">
    <Typography.Title level={4} className="brand-title">
      RouteCraft
    </Typography.Title>
    <Space wrap>
      <Button onClick={onAuth}>Sign in</Button>
      {isAuthenticated && (
        <Button icon={<UserOutlined />} onClick={onMyRoutes}>
          My routes
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
