import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Sales from './sales';
import SalesDetail from './sales-detail';
import SalesUpdate from './sales-update';
import SalesDeleteDialog from './sales-delete-dialog';

const SalesRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Sales />} />
    <Route path="new" element={<SalesUpdate />} />
    <Route path=":id">
      <Route index element={<SalesDetail />} />
      <Route path="edit" element={<SalesUpdate />} />
      <Route path="delete" element={<SalesDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SalesRoutes;
