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
      .regex(/^\w+$/, "Используйте латинские буквы, цифры и _"),
    email: z.email("Введите корректный email"),
    password: z.string().min(8, "Минимум 8 символов").max(64, "Максимум 64 символа"),
    repeatPassword: z.string().min(1, "Повторите пароль"),
    code: z.string().regex(/^\d{6}$/, "Введите код из 6 цифр"),
  })
  .refine((data) => data.password === data.repeatPassword, {
    message: "Пароли не совпадают",
    path: ["repeatPassword"],
  });

export const forgotPasswordSchema = z
  .object({
    email: z.email("Введите корректный email"),
    code: z.string().regex(/^\d{6}$/, "Введите код из 6 цифр"),
    newPassword: z.string().min(8, "Минимум 8 символов").max(64, "Максимум 64 символа"),
    repeatPassword: z.string().min(1, "Повторите пароль"),
  })
  .refine((data) => data.newPassword === data.repeatPassword, {
    message: "Пароли не совпадают",
    path: ["repeatPassword"],
  });

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, "Введите текущий пароль"),
    newPassword: z.string().min(8, "Минимум 8 символов").max(64, "Максимум 64 символа"),
    repeatPassword: z.string().min(1, "Повторите пароль"),
  })
  .refine((data) => data.newPassword === data.repeatPassword, {
    message: "Пароли не совпадают",
    path: ["repeatPassword"],
  });

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.infer<typeof registerSchema>;
export type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;
export type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;
