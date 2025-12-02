import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import PurchaseItems from './purchase-items';
import PurchaseItemsDetail from './purchase-items-detail';
import PurchaseItemsUpdate from './purchase-items-update';
import PurchaseItemsDeleteDialog from './purchase-items-delete-dialog';

const PurchaseItemsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<PurchaseItems />} />
    <Route path="new" element={<PurchaseItemsUpdate />} />
    <Route path=":id">
      <Route index element={<PurchaseItemsDetail />} />
      <Route path="edit" element={<PurchaseItemsUpdate />} />
      <Route path="delete" element={<PurchaseItemsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PurchaseItemsRoutes;
