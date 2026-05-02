import { Button, Layout, Space, Typography } from "antd";
import { RobotOutlined } from "@ant-design/icons";

const { Header } = Layout;

interface HomeTopBarProps {
  onAuth: () => void;
  onCreate: () => void;
  onOpenAiSearch: () => void;
}

export const HomeTopBar = ({ onAuth, onCreate, onOpenAiSearch }: HomeTopBarProps) => (
  <Header className="app-header">
    <Typography.Title level={4} className="brand-title">
      RouteCraft
    </Typography.Title>
    <Space wrap>
      <Button onClick={onAuth}>Авторизоваться</Button>
      <Button type="primary" onClick={onCreate}>
        Создать
      </Button>
      <Button icon={<RobotOutlined />} onClick={onOpenAiSearch}>
        ИИ-поиск
      </Button>
    </Space>
  </Header>
);
