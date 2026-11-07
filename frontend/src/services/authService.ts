import { env } from '../config/env';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/auth';
import { realAuthService } from './realAuthService';
import { mockAuthService } from './mock/mockAuthService';

export interface AuthService {
  login(payload: LoginRequest): Promise<AuthResponse>;
  register(payload: RegisterRequest): Promise<User>;
  getCurrentUser(): Promise<User>;
}

/**
 * Service factory that resolves between Real Backend and Mock Adapter.
 * Constraint: Real API never falls back to Mock on backend failure.
 */
export const authService: AuthService = env.ENABLE_MOCK_API
  ? mockAuthService
  : realAuthService;
