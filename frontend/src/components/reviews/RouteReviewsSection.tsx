import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Form, Input, List, Pagination, Popconfirm, Rate, Spin, Typography, message } from "antd";
import { DeleteOutlined } from "@ant-design/icons";
import { useRoutesStore } from "../../store/routes-store";
import { usersApi } from "../../api/users-api";
import { ApiClientError } from "../../types/api";

interface RouteReviewsSectionProps {
  routeId: string;
}

interface ReviewFormValues {
  rating: number;
  comment: string;
}

const REVIEWS_PAGE_SIZE = 10;

export const RouteReviewsSection = ({ routeId }: RouteReviewsSectionProps) => {
  const [reviewForm] = Form.useForm<ReviewFormValues>();
  const [messageApi, contextHolder] = message.useMessage();
  const [currentUsername, setCurrentUsername] = useState<string | null>(null);

  const reviews = useRoutesStore((state) => state.reviews);
  const reviewsLoading = useRoutesStore((state) => state.reviewsLoading);
  const reviewsPagination = useRoutesStore((state) => state.reviewsPagination);
  const reviewsErrorMessage = useRoutesStore((state) => state.reviewsErrorMessage);
  const creatingReview = useRoutesStore((state) => state.creatingReview);
  const deletingReviewId = useRoutesStore((state) => state.deletingReviewId);
  const fetchReviews = useRoutesStore((state) => state.fetchReviews);
  const createReview = useRoutesStore((state) => state.createReview);
  const deleteReview = useRoutesStore((state) => state.deleteReview);

  const clientRole = useMemo(
    () => localStorage.getItem("role") as "ROLE_USER" | "ROLE_ADMIN" | null,
    [],
  );
  const isAuthenticated = Boolean(localStorage.getItem("accessToken"));

  useEffect(() => {
    void fetchReviews(routeId, { page: 1, limit: REVIEWS_PAGE_SIZE });
  }, [fetchReviews, routeId]);

  useEffect(() => {
    if (!clientRole) {
      setCurrentUsername(null);
      return;
    }

    let isMounted = true;
    void usersApi
      .getCurrentUser()
      .then((user) => {
        if (isMounted) {
          setCurrentUsername(user.username ?? null);
        }
      })
      .catch(() => {
        if (isMounted) {
          setCurrentUsername(null);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [clientRole]);

  const canDeleteReview = (authorName: string) => {
    if (clientRole === "ROLE_ADMIN") {
      return true;
    }
    if (clientRole === "ROLE_USER" && currentUsername) {
      return authorName === currentUsername;
    }
    return false;
  };

  const handleReviewsPageChange = (page: number, pageSize: number) => {
    void fetchReviews(routeId, { page, limit: pageSize });
  };

  const formatReviewDate = (value: string | undefined) => {
    if (!value) {
      return "";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "";
    }
    return date.toLocaleString("ru-RU");
  };

  const handleReviewSubmit = async (values: ReviewFormValues) => {
    try {
      await createReview(routeId, {
        rating: values.rating,
        comment: values.comment.trim(),
      });
      reviewForm.resetFields();
      messageApi.success("Комментарий добавлен");
    } catch (error) {
      if (error instanceof ApiClientError && (error.status === 401 || error.status === 403)) {
        messageApi.warning("Войдите, чтобы оставлять отзывы.");
        return;
      }
      messageApi.error(error instanceof ApiClientError ? error.message : "Не удалось добавить комментарий");
    }
  };

  const handleDeleteReview = async (reviewId: string) => {
    try {
      await deleteReview(routeId, reviewId);
      messageApi.success("Комментарий удален");
    } catch (error) {
      if (error instanceof ApiClientError && (error.status === 401 || error.status === 403)) {
        messageApi.warning("Войдите, чтобы удалять отзывы.");
        return;
      }
      messageApi.error(error instanceof ApiClientError ? error.message : "Не удалось удалить комментарий");
    }
  };

  return (
    <div>
      {contextHolder}

      {isAuthenticated ? (
        <Form<ReviewFormValues>
          form={reviewForm}
          layout="vertical"
          onFinish={(values) => void handleReviewSubmit(values)}
          className="review-form"
          initialValues={{ rating: 5, comment: "" }}
        >
          <Form.Item name="rating" label="Оценка" rules={[{ required: true, message: "Поставьте оценку" }]}>
            <Rate />
          </Form.Item>
          <Form.Item
            name="comment"
            label="Комментарий"
            rules={[
              { required: true, message: "Введите комментарий" },
              { min: 2, message: "Минимум 2 символа" },
              { max: 1000, message: "Максимум 1000 символов" },
            ]}
          >
            <Input.TextArea rows={3} showCount maxLength={1000} />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={creatingReview}>
            Отправить
          </Button>
        </Form>
      ) : (
        <Alert className="review-form" type="info" showIcon message="Войдите, чтобы оставить отзыв." />
      )}

      {reviewsErrorMessage && (
        <Alert className="review-form" type="warning" showIcon message={reviewsErrorMessage} />
      )}

      {reviewsLoading ? (
        <div className="content-loader">
          <Spin />
        </div>
      ) : (
        <>
          <List
            dataSource={reviews}
            locale={{ emptyText: "Комментариев пока нет" }}
            renderItem={(review) => (
              <List.Item
                actions={
                  canDeleteReview(review.authorName)
                    ? [
                        <Popconfirm
                          key={`delete-${review.id}`}
                          title="Удалить комментарий?"
                          okText="Удалить"
                          cancelText="Отмена"
                          onConfirm={() => void handleDeleteReview(review.id)}
                        >
                          <Button
                            danger
                            size="small"
                            icon={<DeleteOutlined />}
                            loading={deletingReviewId === review.id}
                          />
                        </Popconfirm>,
                      ]
                    : []
                }
              >
                <List.Item.Meta
                  title={
                    <div className="review-header">
                      <Typography.Text strong>{review.authorName}</Typography.Text>
                      <Rate disabled value={review.rating} />
                    </div>
                  }
                  description={
                    <div>
                      <Typography.Paragraph style={{ marginBottom: 8 }}>{review.comment}</Typography.Paragraph>
                      <Typography.Text type="secondary">{formatReviewDate(review.createdAt)}</Typography.Text>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
          {reviewsPagination.totalItems > reviewsPagination.itemsPerPage && (
            <div className="pagination-wrap">
              <Pagination
                current={reviewsPagination.currentPage}
                pageSize={reviewsPagination.itemsPerPage}
                total={reviewsPagination.totalItems}
                showSizeChanger={false}
                onChange={handleReviewsPageChange}
              />
            </div>
          )}
        </>
      )}
    </div>
  );
};
