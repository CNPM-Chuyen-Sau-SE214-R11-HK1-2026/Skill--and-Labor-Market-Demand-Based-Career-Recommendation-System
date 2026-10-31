import React, { createContext, useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/auth';
import { authService } from '../services/authService';
import { resetUnauthorizedHandling } from '../services/api';
import { tokenStorage } from '../utils/tokenStorage';
import { isTokenExpired } from '../utils/jwt';

export interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<User>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const navigate = useNavigate();

  // Restore and validate session on initial application load
  useEffect(() => {
    const storedToken = tokenStorage.getToken();
    const storedUser = tokenStorage.getUser();

    if (storedToken && storedUser) {
      if (isTokenExpired(storedToken)) {
        tokenStorage.clearSession();
        setToken(null);
        setUser(null);
      } else {
        setToken(storedToken);
        setUser(storedUser);
      }
    } else {
      tokenStorage.clearSession();
      setToken(null);
      setUser(null);
    }

    setIsLoading(false);
  }, []);

  // Listen for unauthorized 401 events dispatched by Axios Interceptor
  useEffect(() => {
    const handleUnauthorized = () => {
      setToken(null);
      setUser(null);
      navigate('/login', { replace: true });
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, [navigate]);

  const login = useCallback(
    async (payload: LoginRequest): Promise<void> => {
      const response: AuthResponse = await authService.login(payload);
      tokenStorage.saveSession(response);
      setToken(response.accessToken);
      setUser(response.user);
      resetUnauthorizedHandling();
    },
    []
  );

  const register = useCallback(
    async (payload: RegisterRequest): Promise<User> => {
      return await authService.register(payload);
    },
    []
  );

  const logout = useCallback(() => {
    tokenStorage.clearSession();
    setToken(null);
    setUser(null);
    navigate('/login', { replace: true });
  }, [navigate]);

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: Boolean(token && user),
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
