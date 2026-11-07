/**
 * User roles supported by the system.
 * Aligns with GAP-04 and backend Role enum.
 */
export type UserRole = 'ROLE_CANDIDATE' | 'ROLE_ADMIN';

/**
 * User representation returned by auth endpoints.
 * Aligns with backend RegisteredUserResponse / CurrentUserResponse.
 */
export interface User {
  id: number;
  email: string;
  fullName: string;
  role: UserRole;
  status?: string;
  createdAt?: string;
}

/**
 * Payload for POST /api/v1/auth/login.
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Payload for POST /api/v1/auth/register.
 */
export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
}

/**
 * Response structure for successful authentication.
 * Aligns with backend LoginResponse (stateless 24h JWT, no refresh token).
 */
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}
