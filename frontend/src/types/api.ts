export interface FieldValidationError {
  field: string;
  message: string;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path?: string;
}

export interface ValidationErrorResponse extends ErrorResponse {
  validationErrors: FieldValidationError[];
}

const isObject = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null;

export const parseFieldValidationErrors = (value: unknown): FieldValidationError[] => {
  if (!Array.isArray(value)) {
    return [];
  }

  return value
    .map((entry): FieldValidationError | null => {
      if (!isObject(entry)) {
        return null;
      }

      const field = typeof entry.field === "string" ? entry.field : null;
      const message = typeof entry.message === "string" ? entry.message : null;

      if (!field || !message) {
        return null;
      }

      return { field, message };
    })
    .filter((entry): entry is FieldValidationError => entry !== null);
};

export const isErrorResponse = (value: unknown): value is ErrorResponse => {
  if (!isObject(value)) {
    return false;
  }

  return (
    typeof value.status === "number" &&
    typeof value.error === "string" &&
    typeof value.message === "string"
  );
};

export const isValidationErrorResponse = (
  value: unknown,
): value is ValidationErrorResponse => {
  if (!isObject(value) || !isErrorResponse(value)) {
    return false;
  }

  return Array.isArray((value as Record<string, unknown>).validationErrors);
};

export class ApiClientError extends Error {
  public readonly status?: number;
  public readonly error?: string;
  public readonly path?: string;
  public readonly validationErrors: FieldValidationError[];

  public constructor(params: {
    message: string;
    status?: number;
    error?: string;
    path?: string;
    validationErrors?: FieldValidationError[];
  }) {
    super(params.message);
    this.name = "ApiClientError";
    this.status = params.status;
    this.error = params.error;
    this.path = params.path;
    this.validationErrors = params.validationErrors ?? [];
  }
}
