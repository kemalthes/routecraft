import { Col, Empty, Pagination, Row, Spin, Typography } from "antd";
import { RoutePreviewCard } from "../routes/RoutePreviewCard";
import type { PaginationMeta, RoutePreview } from "../../types/routes";

interface HomeRoutesSectionProps {
  loading: boolean;
  routes: RoutePreview[];
  errorMessage: string | null;
  pagination: PaginationMeta;
  togglingFavoriteId?: string | null;
  onOpenRoute: (id: string) => void;
  onPageChange: (page: number, pageSize: number) => void;
  onToggleFavorite?: (id: string, nextLiked: boolean) => void;
}

export const HomeRoutesSection = ({
  loading,
  routes,
  errorMessage,
  pagination,
  togglingFavoriteId,
  onOpenRoute,
  onPageChange,
  onToggleFavorite,
}: HomeRoutesSectionProps) => {
  if (errorMessage) {
    return (
      <Typography.Text type="danger" className="error-text">
        {errorMessage}
      </Typography.Text>
    );
  }

  if (loading) {
    return (
      <div className="content-loader">
        <Spin size="large" />
      </div>
    );
  }

  if (routes.length === 0) {
    return <Empty description="Маршруты не найдены" />;
  }

  return (
    <>
      <Row gutter={[16, 16]}>
        {routes.map((route) => (
          <Col xs={24} sm={12} xl={8} key={route.id}>
            <RoutePreviewCard
              route={route}
              onClick={onOpenRoute}
              onToggleFavorite={onToggleFavorite}
              favoriteLoading={togglingFavoriteId === route.id}
            />
          </Col>
        ))}
      </Row>
      <div className="pagination-wrap">
        <Pagination
          current={pagination.currentPage}
          pageSize={pagination.itemsPerPage}
          total={pagination.totalItems}
          showSizeChanger
          onChange={onPageChange}
        />
      </div>
    </>
  );
};
