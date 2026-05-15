import { z } from "zod";

export const loginSchema = z.object({
  email: z.email("Enter a valid email"),
  password: z.string().min(1, "Enter password"),
});

export const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, "Minimum 3 characters")
      .max(30, "Maximum 30 characters")
      .regex(/^\w+$/, "Use latin letters, digits and _"),
    email: z.email("Enter a valid email"),
    password: z.string().min(8, "Minimum 8 characters").max(64, "Maximum 64 characters"),
    repeatPassword: z.string().min(1, "Repeat password"),
    code: z.string().regex(/^\d{6}$/, "Enter the 6-digit code"),
  })
  .refine((data) => data.password === data.repeatPassword, {
    message: "Passwords do not match",
    path: ["repeatPassword"],
  });

export const forgotPasswordSchema = z
  .object({
    email: z.email("Enter a valid email"),
    code: z.string().regex(/^\d{6}$/, "Enter the 6-digit code"),
    newPassword: z.string().min(8, "Minimum 8 characters").max(64, "Maximum 64 characters"),
    repeatPassword: z.string().min(1, "Repeat password"),
  })
  .refine((data) => data.newPassword === data.repeatPassword, {
    message: "Passwords do not match",
    path: ["repeatPassword"],
  });

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, "Enter current password"),
    newPassword: z.string().min(8, "Minimum 8 characters").max(64, "Maximum 64 characters"),
    repeatPassword: z.string().min(1, "Repeat password"),
  })
  .refine((data) => data.newPassword === data.repeatPassword, {
    message: "Passwords do not match",
    path: ["repeatPassword"],
  });

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.infer<typeof registerSchema>;
export type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;
export type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;
