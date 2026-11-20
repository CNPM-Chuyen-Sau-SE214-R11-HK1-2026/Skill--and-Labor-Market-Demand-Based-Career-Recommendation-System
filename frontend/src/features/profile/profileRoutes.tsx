import type { FeatureRoute } from '../../routes';
import ProfilePage from './pages/ProfilePage';

/**
 * Uses a single parent route with nested children ('/profile' and '/profile/skills')
 * so React Router never unmounts ProfilePage when switching tabs on mobile/tablet,
 * preserving unsaved form state in ProfileEditForm.
 */
const profileRoutes: FeatureRoute[] = [
  {
    path: '/profile',
    element: <ProfilePage />,
    handle: {
      layout: 'candidate' as const,
      roles: ['ROLE_CANDIDATE'],
    },
    children: [
      { index: true, element: null },
      { path: 'skills', element: null },
    ],
  },
];

export default profileRoutes;
