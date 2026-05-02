import { Card, Typography } from "antd";
import type { RoutePreview } from "../../types/routes";

interface RoutePreviewCardProps {
  route: RoutePreview;
  onClick: (id: string) => void;
}

const TRANSPARENT_FALLBACK =
  "data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs=";

export const RoutePreviewCard = ({ route, onClick }: RoutePreviewCardProps) => (
  <Card
    hoverable
    className="route-preview-card"
    onClick={() => onClick(route.id)}
    cover={
      <img
        src={route.imageUrl}
        alt={route.title}
        className="route-preview-image"
        loading="lazy"
        onError={(event) => {
          event.currentTarget.src = TRANSPARENT_FALLBACK;
        }}
      />
    }
  >
    <Typography.Title level={5}>{route.title}</Typography.Title>
    <Typography.Text type="secondary">
      {route.distance.toFixed(1)} км · {route.durationMinutes} мин · {route.authorName}
    </Typography.Text>
  </Card>
);
