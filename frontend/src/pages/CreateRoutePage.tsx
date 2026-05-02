import { Link, useNavigate } from "react-router-dom";
import { Button, Card, Layout, Space, Typography } from "antd";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { RouteForm } from "../components/routes/RouteForm";

const { Content } = Layout;

export const CreateRoutePage = () => {
  const navigate = useNavigate();

  return (
    <Layout className="create-layout">
      <Content className="create-content">
        <Space style={{ marginBottom: 16 }}>
          <Link to="/">
            <Button icon={<ArrowLeftOutlined />}>На главную</Button>
          </Link>
        </Space>

        <Card>
          <Typography.Title level={3}>Создание маршрута</Typography.Title>
          <RouteForm onCreated={() => navigate("/")} />
        </Card>
      </Content>
    </Layout>
  );
};
