import { useMemo, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { Button, Form, Input, Space, Typography, Upload, message } from "antd";
import type { UploadFile } from "antd";
import { InboxOutlined } from "@ant-design/icons";
import { ApiClientError } from "../../types/api";
import { useRoutesStore } from "../../store/routes-store";
import type { CreateRouteFormValues, RouteLocationFormPoint } from "../../types/routes";
import { createRouteFormSchema } from "../../schema/route-form.schema";
import { RoutePointsMap } from "./RoutePointsMap";
import { RoutePointsList } from "./RoutePointsList";

interface RouteFormProps {
  onCreated?: (routeId: string) => Promise<void> | void;
}

export const RouteForm = ({ onCreated }: RouteFormProps) => {
  const createRoute = useRoutesStore((state) => state.createRoute);
  const creating = useRoutesStore((state) => state.creating);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  const {
    control,
    handleSubmit,
    setValue,
    getValues,
    setError,
    reset,
    watch,
    formState: { errors },
  } = useForm<CreateRouteFormValues>({
    resolver: zodResolver(createRouteFormSchema),
    defaultValues: {
      title: "",
      description: "",
      locations: [],
    },
    mode: "onSubmit",
  });

  const locations = watch("locations");

  const uploadFileList = useMemo<UploadFile[]>(
    () =>
      selectedFile
        ? [
            {
              uid: "route-image",
              name: selectedFile.name,
              status: "done",
              size: selectedFile.size,
              type: selectedFile.type,
            },
          ]
        : [],
    [selectedFile],
  );

  const setLocations = (points: RouteLocationFormPoint[]) => {
    setValue(
      "locations",
      points.map((point, index) => ({ ...point, orderIndex: index })),
      {
        shouldDirty: true,
        shouldValidate: true,
      },
    );
  };

  const handleMapClick = (lat: number, lng: number) => {
    const current = getValues("locations");
    setLocations([...current, { orderIndex: current.length, lat, lng }]);
  };

  const removeLocation = (indexToRemove: number) => {
    const updated = getValues("locations").filter((_, index) => index !== indexToRemove);
    setLocations(updated);
  };

  const onSubmit = handleSubmit(async (values) => {
    if (!selectedFile) {
      messageApi.error("Добавьте изображение маршрута");
      return;
    }

    try {
      const routeId = await createRoute(values, selectedFile);
      messageApi.success("Маршрут создан. Изображение загружено в MinIO.");
      reset();
      setSelectedFile(null);
      await onCreated?.(routeId);
    } catch (error) {
      if (error instanceof ApiClientError) {
        if (error.validationErrors.length > 0) {
          error.validationErrors.forEach((validationError) => {
            if (validationError.field === "title") {
              setError("title", { message: validationError.message });
              return;
            }
            if (validationError.field === "description") {
              setError("description", { message: validationError.message });
              return;
            }
            if (validationError.field.includes("locations")) {
              setError("locations", { message: validationError.message });
            }
          });
        }
        messageApi.error(error.message);
        return;
      }

      messageApi.error("Не удалось создать маршрут");
    }
  });

  return (
    <div>
      {contextHolder}
      <Space orientation="vertical" size={16} style={{ width: "100%" }}>
        <Typography.Title level={5} style={{ margin: 0 }}>
          Создание маршрута
        </Typography.Title>

        <Form layout="vertical" onFinish={onSubmit} requiredMark={false}>
          <Form.Item label="Название" validateStatus={errors.title ? "error" : ""} help={errors.title?.message}>
            <Controller
              name="title"
              control={control}
              render={({ field }) => <Input {...field} placeholder="Название маршрута" />}
            />
          </Form.Item>

          <Form.Item
            label="Описание"
            validateStatus={errors.description ? "error" : ""}
            help={errors.description?.message}
          >
            <Controller
              name="description"
              control={control}
              render={({ field }) => <Input.TextArea {...field} rows={4} placeholder="Описание" />}
            />
          </Form.Item>

          <Form.Item label="Фото маршрута">
            <Upload.Dragger
              accept="image/*"
              maxCount={1}
              multiple={false}
              fileList={uploadFileList}
              beforeUpload={(file) => {
                setSelectedFile(file);
                return false;
              }}
              onRemove={() => {
                setSelectedFile(null);
                return true;
              }}
            >
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">Кликните или перетащите файл</p>
            </Upload.Dragger>
          </Form.Item>

          <Form.Item
            label="Точки маршрута (клик по карте)"
            validateStatus={errors.locations ? "error" : ""}
            help={errors.locations?.message}
          >
            <RoutePointsMap points={locations} onMapClick={handleMapClick} />
          </Form.Item>

          <RoutePointsList points={locations} onRemovePoint={removeLocation} />

          <Button type="primary" htmlType="submit" loading={creating} style={{ marginTop: 16 }} block>
            Создать маршрут
          </Button>
        </Form>
      </Space>
    </div>
  );
};
