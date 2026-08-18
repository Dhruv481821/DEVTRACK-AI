// Matches the response envelope defined in /docs/06_API_Specification.md §1.3.
// Real request/response DTO types are generated from the backend's OpenAPI spec
// (`npm run gen:api-types`, per /docs/08_Frontend_Architecture.md §3) once the
// backend exposes real endpoints beyond Phase 0 scaffolding — this file only
// defines the envelope shape itself, which isn't generated.

export interface ApiSuccessEnvelope<T> {
  success: true;
  data: T;
  meta: {
    timestamp: string;
    pagination?: {
      page: number;
      size: number;
      totalElements: number;
      totalPages: number;
    };
  };
}

export interface ApiErrorEnvelope {
  success: false;
  error: {
    code: string;
    message: string;
    details?: Array<{ field: string; reason: string }>;
  };
  meta: { timestamp: string };
}

export type ApiEnvelope<T> = ApiSuccessEnvelope<T> | ApiErrorEnvelope;

/** Thrown by the api client on any non-2xx response — carries the envelope's error. */
export class ApiError extends Error {
  code: string;
  details?: Array<{ field: string; reason: string }>;

  constructor(envelope: ApiErrorEnvelope) {
    super(envelope.error.message);
    this.name = 'ApiError';
    this.code = envelope.error.code;
    this.details = envelope.error.details;
  }
}
