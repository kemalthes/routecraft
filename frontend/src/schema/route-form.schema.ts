import { z } from "zod";

export const createRouteFormSchema = z.object({
  title: z.string().trim().min(5, "Минимум 5 символов").max(100, "Максимум 100 символов"),
  description: z.string().max(2000, "Максимум 2000 символов"),
  locations: z
    .array(
      z.object({
        orderIndex: z.number().int().min(0),
        lat: z.number().min(-90).max(90),
        lng: z.number().min(-180).max(180),
      }),
    )
    .min(2, "Нужно минимум 2 точки маршрута"),
});
