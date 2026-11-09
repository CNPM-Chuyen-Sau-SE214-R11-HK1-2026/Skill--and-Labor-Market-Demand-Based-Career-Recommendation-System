import type { ReactElement } from 'react';
import AdminDashboardStub from './AdminDashboardStub';

type AppRouteHandle = { layout: 'candidate' | 'admin' | 'public'; roles?: ('ROLE_CANDIDATE' | 'ROLE_ADMIN')[] };
type FeatureRoute = { path: string; element: ReactElement; handle: AppRouteHandle };

const adminRoutes: FeatureRoute[] = [
  {
    path: '/admin',
    element: <AdminDashboardStub />,
    handle: { layout: 'admin', roles: ['ROLE_ADMIN'] },
  },
];

export default adminRoutes;
