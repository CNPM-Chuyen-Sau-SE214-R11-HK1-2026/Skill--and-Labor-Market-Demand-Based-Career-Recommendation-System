import type { ReactElement } from 'react';
import { NotFoundPage } from '../../pages/NotFoundPage';
import AdminHome from './AdminHome';
import LmiPage from './LmiPage';
import TaxonomyPage from './TaxonomyPage';
import UsersPage from './UsersPage';
type FeatureRoute = {
    path: string;
    element: ReactElement;
    handle: {
        layout: 'admin';
        roles: [
            'ROLE_ADMIN'
        ];
    };
};
const adminRoutes: FeatureRoute[] = [
    { path: '/admin', element: <AdminHome />, handle: { layout: 'admin', roles: ['ROLE_ADMIN'] } },
    { path: '/admin/lmi', element: <LmiPage />, handle: { layout: 'admin', roles: ['ROLE_ADMIN'] } },
    { path: '/admin/taxonomy', element: <TaxonomyPage />, handle: { layout: 'admin', roles: ['ROLE_ADMIN'] } },
    { path: '/admin/users', element: <UsersPage />, handle: { layout: 'admin', roles: ['ROLE_ADMIN'] } },
    { path: '/admin/*', element: <NotFoundPage />, handle: { layout: 'admin', roles: ['ROLE_ADMIN'] } },
];
export default adminRoutes;
