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

/**
 * Thrown by the api client on any non-2xx response — carries the envelope's error.
 *
 * status/headers were added for the AI Service Layer's quota-exceeded response (a 429 with a
 * Retry-After header, not a JSON field — see 09_AI_Architecture.md §7's design review): the
 * envelope alone can't carry that, since a header lives on the raw fetch Response, which the api
 * client previously discarded before constructing this error. Kept fully generic rather than
 * naming a "retryAfterSeconds" field here specifically — any future error that needs any header
 * can read it the same way, and this file gains no feature-specific knowledge.
 */
export class ApiError extends Error {
  code: string;
  details?: Array<{ field: string; reason: string }>;
  status: number;
  headers: Headers;

  constructor(envelope: ApiErrorEnvelope, status: number, headers: Headers) {
    super(envelope.error.message);
    this.name = 'ApiError';
    this.code = envelope.error.code;
    this.details = envelope.error.details;
    this.status = status;
    this.headers = headers;
  }
}