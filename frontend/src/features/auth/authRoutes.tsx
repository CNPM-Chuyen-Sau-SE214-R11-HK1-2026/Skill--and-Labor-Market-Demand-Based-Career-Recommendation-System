import type { ReactElement } from 'react';
import { LoginPage } from './LoginPage';
import { RegisterPage } from './RegisterPage';
import { GuestRoute } from '../../routes/GuestRoute';

type AppRouteHandle = { layout: 'candidate' | 'admin' | 'public'; roles?: ('ROLE_CANDIDATE' | 'ROLE_ADMIN')[] };
type FeatureRoute = { path: string; element: ReactElement; handle: AppRouteHandle };

const authRoutes: FeatureRoute[] = [
  {
    path: '/login',
    element: (
      <GuestRoute>
        <LoginPage />
      </GuestRoute>
    ),
    handle: { layout: 'candidate' },
  },
  {
    path: '/register',
    element: (
      <GuestRoute>
        <RegisterPage />
      </GuestRoute>
    ),
    handle: { layout: 'candidate' },
  },
];

export default authRoutes;
