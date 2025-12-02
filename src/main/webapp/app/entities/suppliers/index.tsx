import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Suppliers from './suppliers';
import SuppliersDetail from './suppliers-detail';
import SuppliersUpdate from './suppliers-update';
import SuppliersDeleteDialog from './suppliers-delete-dialog';

const SuppliersRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Suppliers />} />
    <Route path="new" element={<SuppliersUpdate />} />
    <Route path=":id">
      <Route index element={<SuppliersDetail />} />
      <Route path="edit" element={<SuppliersUpdate />} />
      <Route path="delete" element={<SuppliersDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SuppliersRoutes;
