import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import SaleItems from './sale-items';
import SaleItemsDetail from './sale-items-detail';
import SaleItemsUpdate from './sale-items-update';
import SaleItemsDeleteDialog from './sale-items-delete-dialog';

const SaleItemsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<SaleItems />} />
    <Route path="new" element={<SaleItemsUpdate />} />
    <Route path=":id">
      <Route index element={<SaleItemsDetail />} />
      <Route path="edit" element={<SaleItemsUpdate />} />
      <Route path="delete" element={<SaleItemsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SaleItemsRoutes;
