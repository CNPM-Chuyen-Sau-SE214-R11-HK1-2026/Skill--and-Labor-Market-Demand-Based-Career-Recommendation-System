import React from 'react';
import { createBrowserRouter, Outlet } from 'react-router-dom';
import type { RouteObject } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { CandidateLayout } from '../layouts/CandidateLayout';
import { AdminLayout } from '../layouts/AdminLayout';
import { HomePage } from '../pages/home/HomePage';
import { DashboardPage } from '../pages/candidate/DashboardPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { ProtectedRoute } from './ProtectedRoute';
import type { UserRole } from '../types/auth';

export interface AppRouteHandle {
  layout?: 'candidate' | 'admin' | 'public';
  roles?: UserRole[];
}

export type FeatureRoute = RouteObject & {
  handle?: AppRouteHandle;
};

/**
 * Dynamic module scanning via Vite's import.meta.glob.
 * Adheres to ADR-0008: automatically loads feature routes defined in features/[module]
 */

const featureModules = import.meta.glob<{ default: FeatureRoute[] }>(
  '../features/*/*Routes.tsx',
  { eager: true }
);

const featureRoutes: FeatureRoute[] = Object.values(featureModules).flatMap(
  (m) => m.default || []
);

/**
 * Normalizes route path (stripping leading slash for clean nesting under pathless layout)
 * and wraps element with ProtectedRoute when roles prop is defined.
 */
function normalizeRoute(route: FeatureRoute): RouteObject {
  const roles = route.handle?.roles;
  const element =
    roles && roles.length > 0 && route.element ? (
      <ProtectedRoute roles={roles}>{route.element}</ProtectedRoute>
    ) : (
      route.element
    );

  const path = route.path?.startsWith('/') ? route.path.slice(1) : route.path;

  return {
    ...route,
    path,
    element,
  };
}

// Split routes by layout metadata
const candidateFeatureRoutes = featureRoutes
  .filter((r) => !r.handle?.layout || r.handle.layout === 'candidate' || r.handle.layout === 'public')
  .map(normalizeRoute);

const adminFeatureRoutes = featureRoutes
  .filter((r) => r.handle?.layout === 'admin')
  .map(normalizeRoute);

/**
 * Root component that places AuthProvider inside Router context.
 * Enables AuthContext to safely access useNavigate and useLocation without runtime errors.
 */
const Root: React.FC = () => {
  return (
    <AuthProvider>
      <Outlet />
    </AuthProvider>
  );
};

export const router = createBrowserRouter([
  {
    element: <Root />,
    children: [
      {
        element: <CandidateLayout />,
        children: [
          { index: true, element: <HomePage /> },
          {
            path: 'dashboard',
            element: (
              <ProtectedRoute roles={['ROLE_CANDIDATE', 'ROLE_ADMIN']}>
                <DashboardPage />
              </ProtectedRoute>
            ),
          },
          ...candidateFeatureRoutes,
          { path: '*', element: <NotFoundPage /> },
        ],
      },
      {
        path: 'admin',
        element: (
          <ProtectedRoute roles={['ROLE_ADMIN']}>
            <AdminLayout />
          </ProtectedRoute>
        ),
        children: adminFeatureRoutes.map((r) => {
          // Inside /admin parent, strip 'admin/' prefix if present
          let childPath = r.path;
          if (childPath === 'admin') {
            return { index: true, element: r.element };
          }
          if (childPath?.startsWith('admin/')) {
            childPath = childPath.slice('admin/'.length);
          }
          return { ...r, path: childPath };
        }),
      },
    ],
  },
]);

export default router;
