import { Button, List } from "antd";
import { DeleteOutlined } from "@ant-design/icons";
import type { RouteLocationFormPoint } from "../../types/routes";

interface RoutePointsListProps {
  points: RouteLocationFormPoint[];
  onRemovePoint: (index: number) => void;
}

export const RoutePointsList = ({ points, onRemovePoint }: RoutePointsListProps) => (
  <List
    size="small"
    dataSource={points}
    bordered
    locale={{ emptyText: "Точки не добавлены" }}
    renderItem={(point, index) => (
      <List.Item
        actions={[
          <Button
            key={`remove-${point.orderIndex}`}
            type="text"
            icon={<DeleteOutlined />}
            onClick={() => onRemovePoint(index)}
          />,
        ]}
      >
        #{point.orderIndex + 1}: {point.lat.toFixed(5)}, {point.lng.toFixed(5)}
      </List.Item>
    )}
  />
);
