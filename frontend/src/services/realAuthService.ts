import { apiClient } from './api';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/auth';

/**
 * Real backend authentication service connecting via apiClient.
 * Adheres to GAP-01: relative paths from baseURL /api/v1.
 */
export const realAuthService = {
  async login(payload: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/auth/login', payload);
    return response.data;
  },

  async register(payload: RegisterRequest): Promise<User> {
    const response = await apiClient.post<User>('/auth/register', payload);
    return response.data;
  },

  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<User>('/auth/me');
    return response.data;
  },
};
