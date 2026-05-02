import { Layout, Typography } from "antd";

const { Sider } = Layout;

export const HomeInfoSider = () => (
  <Sider width={280} className="home-sider">
    <Typography.Title level={5}>О сервисе</Typography.Title>
    <Typography.Paragraph>
      RouteCraft помогает публиковать и находить туристические маршруты с точками на карте,
      дистанцией, длительностью и превью.
    </Typography.Paragraph>
    <Typography.Paragraph>
      На главной странице доступен поиск и постраничный просмотр маршрутов. Открой карточку, чтобы
      увидеть полное описание и путь на карте.
    </Typography.Paragraph>
  </Sider>
);
