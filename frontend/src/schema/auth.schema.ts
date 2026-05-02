import { z } from "zod";

export const loginSchema = z.object({
  email: z.email("Введите корректный email"),
  password: z.string().min(1, "Введите пароль"),
});

export const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, "Минимум 3 символа")
      .max(30, "Максимум 30 символов")
      .regex(/^\w+$/, "Разрешены латиница, цифры и _"),
    email: z.email("Введите корректный email"),
    password: z.string().min(8, "Минимум 8 символов").max(64, "Максимум 64 символа"),
    repeatPassword: z.string().min(1, "Повторите пароль"),
  })
  .refine((data) => data.password === data.repeatPassword, {
    message: "Пароли не совпадают",
    path: ["repeatPassword"],
  });

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.infer<typeof registerSchema>;
