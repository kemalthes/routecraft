import { Layout, Typography } from "antd";

const { Sider } = Layout;

export const HomeInfoSider = () => (
  <Sider width={280} className="home-sider">
    <Typography.Title level={5}>О сервисе</Typography.Title>
    <Typography.Paragraph>
      RouteCraft помогает публиковать и находить туристические маршруты с точками на карте,
      дистанцией, длительностью и изображением.
    </Typography.Paragraph>
    <Typography.Paragraph>
      На главной странице доступен поиск и постраничный просмотр маршрутов. Откройте карточку,
      чтобы увидеть полное описание, отзывы и путь на карте.
    </Typography.Paragraph>
  </Sider>
);
