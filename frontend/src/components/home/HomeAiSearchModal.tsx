import { Input, Modal } from "antd";

interface HomeAiSearchModalProps {
  open: boolean;
  query: string;
  loading?: boolean;
  onChangeQuery: (value: string) => void;
  onApply: () => void;
  onClose: () => void;
}

export const HomeAiSearchModal = ({
  open,
  query,
  loading,
  onChangeQuery,
  onApply,
  onClose,
}: HomeAiSearchModalProps) => (
  <Modal
    title="ИИ-поиск маршрутов"
    open={open}
    okText="Найти"
    cancelText="Отмена"
    confirmLoading={loading}
    onOk={onApply}
    onCancel={onClose}
  >
    <Input.TextArea
      rows={4}
      placeholder="Например: спокойный маршрут у воды на 2-3 часа"
      value={query}
      onChange={(event) => onChangeQuery(event.target.value)}
    />
  </Modal>
);
