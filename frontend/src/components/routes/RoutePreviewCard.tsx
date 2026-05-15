import { Button, Card, Typography } from "antd";
import { HeartFilled, HeartOutlined } from "@ant-design/icons";
import type { RoutePreview } from "../../types/routes";

interface RoutePreviewCardProps {
  route: RoutePreview;
  onClick: (id: string) => void;
  onToggleFavorite?: (id: string, nextLiked: boolean) => void;
  favoriteLoading?: boolean;
}

const TRANSPARENT_FALLBACK =
  "data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs=";

export const RoutePreviewCard = ({
  route,
  onClick,
  onToggleFavorite,
  favoriteLoading,
}: RoutePreviewCardProps) => (
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
    <div className="route-card-title-row">
      <Typography.Title level={5}>{route.title}</Typography.Title>
      {onToggleFavorite && (
        <Button
          aria-label={route.is_liked ? "Убрать из избранного" : "Добавить в избранное"}
          icon={route.is_liked ? <HeartFilled /> : <HeartOutlined />}
          loading={favoriteLoading}
          shape="circle"
          type={route.is_liked ? "primary" : "default"}
          onClick={(event) => {
            event.stopPropagation();
            onToggleFavorite(route.id, !route.is_liked);
          }}
        />
      )}
    </div>
    <Typography.Text type="secondary">
      {route.distance.toFixed(1)} км | {route.durationMinutes} мин | {route.authorName}
    </Typography.Text>
  </Card>
);
