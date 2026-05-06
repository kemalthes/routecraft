import { useEffect, useMemo } from "react";
import { Link, useParams } from "react-router-dom";
import { Button, Card, Descriptions, Image, Layout, Spin, Typography } from "antd";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { MapContainer, Polyline, TileLayer, useMap } from "react-leaflet";
import L from "leaflet";
import { useRoutesStore } from "../store/routes-store";
import { RouteReviewsSection } from "../components/reviews/RouteReviewsSection";

const { Content } = Layout;

const RouteBounds = ({ positions }: { positions: [number, number][] }) => {
  const map = useMap();

  useEffect(() => {
    if (positions.length < 2) {
      return;
    }

    map.fitBounds(L.latLngBounds(positions), {
      padding: [48, 48],
      animate: true,
      duration: 0.8,
    });
  }, [map, positions]);

  return null;
};

export const RouteDetailsPage = () => {
  const { id } = useParams<{ id: string }>();
  const selectedRoute = useRoutesStore((state) => state.selectedRoute);
  const routeLoading = useRoutesStore((state) => state.routeLoading);
  const errorMessage = useRoutesStore((state) => state.errorMessage);
  const selectRoute = useRoutesStore((state) => state.selectRoute);

  useEffect(() => {
    if (id) {
      void selectRoute(id);
    }
  }, [id, selectRoute]);

  const positions = useMemo<[number, number][]>(
    () => selectedRoute?.locations.map((point) => [point.latitude, point.longitude]) ?? [],
    [selectedRoute?.locations],
  );

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
          <Typography.Title level={3} style={{ marginTop: 16 }}>
            {selectedRoute.title}
          </Typography.Title>
          <Typography.Paragraph>{selectedRoute.description}</Typography.Paragraph>
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="Автор">{selectedRoute.authorName}</Descriptions.Item>
            <Descriptions.Item label="Дистанция">{selectedRoute.distance.toFixed(1)} км</Descriptions.Item>
            <Descriptions.Item label="Длительность">{selectedRoute.durationMinutes} мин</Descriptions.Item>
            <Descriptions.Item label="Точек маршрута">{selectedRoute.locations.length}</Descriptions.Item>
          </Descriptions>
        </Card>

        <Card title="Маршрут на карте">
          <div className="details-map">
            <MapContainer center={[55.751244, 37.618423]} zoom={11} style={{ width: "100%", height: "100%" }}>
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {positions.length >= 2 && (
                <>
                  <RouteBounds positions={positions} />
                  <Polyline positions={positions} pathOptions={{ color: "#1677ff", weight: 5 }} />
                </>
              )}
            </MapContainer>
          </div>
        </Card>

        {id && (
          <Card title="Комментарии">
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
      <Content className="details-content">
        <div className="details-header">
          <Link to="/">
            <Button icon={<ArrowLeftOutlined />}>На главную</Button>
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
