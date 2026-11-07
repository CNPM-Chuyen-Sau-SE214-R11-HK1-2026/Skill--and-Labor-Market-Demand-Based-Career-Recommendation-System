import React from 'react';
import { Navigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import type { UserRole } from '../types/auth';

export interface ProtectedRouteProps {
  roles?: UserRole[];
  children: React.ReactNode;
}

/**
 * Route guard that ensures authenticated users with valid roles can access the route.
 * Constraint AUTH-04: Checks isLoading before redirecting to prevent flash redirect on F5.
 * Adheres to ADR-0008 Section 3.6: Role matching against 'ROLE_CANDIDATE' | 'ROLE_ADMIN'.
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ roles, children }) => {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingSpinner text="Đang kiểm tra phiên đăng nhập..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles && roles.length > 0) {
    const hasRequiredRole = Boolean(user?.role && roles.includes(user.role));
    if (!hasRequiredRole) {
      return (
        <div style={{ maxWidth: '600px', margin: '4rem auto', textAlign: 'center', padding: '2rem' }}>
          <h2 style={{ fontSize: '1.75rem', marginBottom: '1rem', color: 'var(--color-error, #ef4444)' }}>
            403 - Không có quyền truy cập
          </h2>
          <p style={{ color: 'var(--color-text-muted, #64748b)', marginBottom: '1.5rem' }}>
            Tài khoản của bạn ({user?.role || 'Chưa xác định'}) không có quyền truy cập vào đường dẫn này.
          </p>
          <Link
            to="/dashboard"
            style={{
              display: 'inline-block',
              padding: '0.625rem 1.25rem',
              backgroundColor: 'var(--color-primary, #2563eb)',
              color: '#ffffff',
              borderRadius: 'var(--radius-md, 0.5rem)',
              textDecoration: 'none',
              fontWeight: 500,
            }}
          >
            Quay lại Bảng điều khiển
          </Link>
        </div>
      );
    }
  }

  return <>{children}</>;
};
