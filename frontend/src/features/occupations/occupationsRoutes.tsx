import type { ReactElement } from 'react';
import OccupationListPage from './pages/OccupationListPage';
import OccupationDetailPage from './pages/OccupationDetailPage';

type AppRouteHandle = { layout: 'candidate' | 'admin' | 'public'; roles?: ('ROLE_CANDIDATE' | 'ROLE_ADMIN')[] };
type FeatureRoute = { path: string; element: ReactElement; handle: AppRouteHandle };

const occupationsRoutes: FeatureRoute[] = [
  { path: '/occupations', element: <OccupationListPage />, handle: { layout: 'candidate', roles: [] } },
  { path: '/occupations/:id', element: <OccupationDetailPage />, handle: { layout: 'candidate', roles: [] } },
];

export default occupationsRoutes;
