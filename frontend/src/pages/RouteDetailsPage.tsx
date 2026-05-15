import { useEffect } from "react";
import { Link, useParams } from "react-router-dom";
import { Button, Card, Descriptions, Image, Layout, Spin, Typography, message } from "antd";
import { ArrowLeftOutlined, HeartFilled, HeartOutlined } from "@ant-design/icons";
import { useRoutesStore } from "../store/routes-store";
import { RouteReviewsSection } from "../components/reviews/RouteReviewsSection";
import { RouteReadonlyMap } from "../components/routes/RouteReadonlyMap";

const { Content } = Layout;

export const RouteDetailsPage = () => {
  const { id } = useParams<{ id: string }>();
  const [messageApi, contextHolder] = message.useMessage();
  const selectedRoute = useRoutesStore((state) => state.selectedRoute);
  const routeLoading = useRoutesStore((state) => state.routeLoading);
  const togglingFavoriteId = useRoutesStore((state) => state.togglingFavoriteId);
  const errorMessage = useRoutesStore((state) => state.errorMessage);
  const selectRoute = useRoutesStore((state) => state.selectRoute);
  const toggleFavorite = useRoutesStore((state) => state.toggleFavorite);

  useEffect(() => {
    if (id) {
      void selectRoute(id);
    }
  }, [id, selectRoute]);

  let content;
  if (routeLoading) {
    content = (
      <div className="content-loader">
        <Spin size="large" />
      </div>
    );
  } else if (errorMessage) {
    content = <Typography.Text type="danger">{errorMessage}</Typography.Text>;
  } else if (selectedRoute) {
    content = (
      <div className="details-grid">
        <Card>
          <Image src={selectedRoute.imageUrl} alt={selectedRoute.title} className="details-image" />
          <div className="details-title-row">
            <Typography.Title level={3} style={{ marginTop: 16 }}>
              {selectedRoute.title}
            </Typography.Title>
            <Button
              icon={selectedRoute.is_liked ? <HeartFilled /> : <HeartOutlined />}
              loading={togglingFavoriteId === selectedRoute.id}
              type={selectedRoute.is_liked ? "primary" : "default"}
              onClick={() => {
                if (!localStorage.getItem("accessToken")) {
                  messageApi.warning("Войдите, чтобы добавлять маршруты в избранное.");
                  return;
                }
                void toggleFavorite(selectedRoute.id, !selectedRoute.is_liked).catch(() => {
                  messageApi.error("Не удалось обновить избранное. Обновите страницу и попробуйте снова.");
                });
              }}
            >
              {selectedRoute.is_liked ? "В избранном" : "В избранное"}
            </Button>
          </div>
          <Typography.Paragraph>{selectedRoute.description}</Typography.Paragraph>
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="Автор">{selectedRoute.authorName}</Descriptions.Item>
            <Descriptions.Item label="Дистанция">{selectedRoute.distance.toFixed(1)} км</Descriptions.Item>
            <Descriptions.Item label="Время">{selectedRoute.durationMinutes} мин</Descriptions.Item>
            <Descriptions.Item label="Точки">{selectedRoute.locations.length}</Descriptions.Item>
          </Descriptions>
        </Card>

        <Card title="Карта маршрута">
          <div className="details-map">
            <RouteReadonlyMap locations={selectedRoute.locations} geometry={selectedRoute.geometry} />
          </div>
        </Card>

        {id && (
          <Card title="Отзывы">
            <RouteReviewsSection routeId={id} />
          </Card>
        )}
      </div>
    );
  } else {
    content = <Typography.Text>Маршрут не найден</Typography.Text>;
  }

  return (
    <Layout className="details-layout">
      {contextHolder}
      <Content className="details-content">
        <div className="details-header">
          <Link to="/">
            <Button icon={<ArrowLeftOutlined />}>Назад</Button>
          </Link>
          <Link to="/create">
            <Button type="primary">Создать маршрут</Button>
          </Link>
        </div>
        {content}
      </Content>
    </Layout>
  );
};
