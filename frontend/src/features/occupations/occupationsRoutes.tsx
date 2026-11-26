import type { FeatureRoute } from '../../routes';
import OccupationListPage from './pages/OccupationListPage';
import OccupationDetailPage from './pages/OccupationDetailPage';

const occupationsRoutes: FeatureRoute[] = [
  { path: '/occupations', element: <OccupationListPage />, handle: { layout: 'candidate' as const, roles: [] } },
  { path: '/occupations/:id', element: <OccupationDetailPage />, handle: { layout: 'candidate' as const, roles: [] } },
];

export default occupationsRoutes;
