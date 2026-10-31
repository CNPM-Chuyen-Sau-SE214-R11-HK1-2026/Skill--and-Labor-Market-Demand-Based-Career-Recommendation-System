import { AuthResponse, LoginRequest, RegisterRequest, User } from '../../types/auth';

/**
 * Isolated mock auth service.
 * Used ONLY when VITE_ENABLE_MOCK_API=true.
 * Generates distinct mock tokens and never communicates with backend.
 */
export const mockAuthService = {
  async login(payload: LoginRequest): Promise<AuthResponse> {
    await new Promise((resolve) => setTimeout(resolve, 300));

    // Simple validation in mock mode
    if (payload.password === 'WrongPassword!') {
      const error: Error & { response?: { status: number; data: { message: string } } } =
        new Error('Unauthorized');
      error.response = {
        status: 401,
        data: { message: 'Sai thông tin đăng nhập hoặc tài khoản bị khóa' },
      };
      throw error;
    }

    const isAdmin = payload.email.toLowerCase().includes('admin');
    const mockUser: User = {
      id: isAdmin ? 99 : 1,
      email: payload.email,
      fullName: isAdmin ? 'Admin Mock User' : 'Mock Candidate User',
      role: isAdmin ? 'ROLE_ADMIN' : 'ROLE_CANDIDATE',
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
    };

    return {
      accessToken: `mock-token-${isAdmin ? 'admin' : 'candidate'}-${Date.now()}`,
      tokenType: 'Bearer',
      expiresIn: 86400,
      user: mockUser,
    };
  },

  async register(payload: RegisterRequest): Promise<User> {
    await new Promise((resolve) => setTimeout(resolve, 300));

    if (payload.email === 'existing@example.com') {
      const error: Error & { response?: { status: number; data: { message: string } } } =
        new Error('Conflict');
      error.response = {
        status: 409,
        data: { message: 'Email đã tồn tại trong hệ thống' },
      };
      throw error;
    }

    return {
      id: Math.floor(Math.random() * 1000) + 10,
      email: payload.email,
      fullName: payload.fullName,
      role: 'ROLE_CANDIDATE',
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
    };
  },

  async getCurrentUser(): Promise<User> {
    await new Promise((resolve) => setTimeout(resolve, 150));
    return {
      id: 1,
      email: 'mock.candidate@example.com',
      fullName: 'Mock Candidate User',
      role: 'ROLE_CANDIDATE',
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
    };
  },
};
