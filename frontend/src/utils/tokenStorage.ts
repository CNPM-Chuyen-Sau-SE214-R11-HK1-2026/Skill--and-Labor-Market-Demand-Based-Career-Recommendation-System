import type { AuthResponse, User } from '../types/auth';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';

/**
 * Centralized session and token storage manager.
 *
 * Security Note:
 * localStorage is accessible to client-side JavaScript and subject to XSS risks.
 * This is a pragmatic baseline for the current stateless Bearer JWT architecture (24h validity).
 * It will be evaluated for upgrade to httpOnly cookies and refresh token rotation in future phases.
 */
export const tokenStorage = {
  getToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },

  getUser(): User | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      if (!raw) return null;
      return JSON.parse(raw) as User;
    } catch {
      // Safe cleanup if corrupted JSON is found
      try {
        localStorage.removeItem(USER_KEY);
      } catch {
        // Ignore storage access error
      }
      return null;
    }
  },

  saveSession(data: AuthResponse): void {
    try {
      localStorage.setItem(TOKEN_KEY, data.accessToken);
      localStorage.setItem(USER_KEY, JSON.stringify(data.user));
    } catch {
      // Fail gracefully if storage is full or disabled
    }
  },

  clearSession(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    } catch {
      // Fail gracefully
    }
  },
};
