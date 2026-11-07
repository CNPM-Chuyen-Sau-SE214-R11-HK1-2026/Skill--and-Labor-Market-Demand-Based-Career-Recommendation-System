import React from 'react';
import { RouterProvider } from 'react-router-dom';
import { router } from './index';

/**
 * AppRoutes wrapper component.
 * Re-exports the router provider for backward compatibility.
 */
export const AppRoutes: React.FC = () => {
  return <RouterProvider router={router} />;
};

export default AppRoutes;
