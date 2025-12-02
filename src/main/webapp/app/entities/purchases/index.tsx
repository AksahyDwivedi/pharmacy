import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Purchases from './purchases';
import PurchasesDetail from './purchases-detail';
import PurchasesUpdate from './purchases-update';
import PurchasesDeleteDialog from './purchases-delete-dialog';

const PurchasesRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Purchases />} />
    <Route path="new" element={<PurchasesUpdate />} />
    <Route path=":id">
      <Route index element={<PurchasesDetail />} />
      <Route path="edit" element={<PurchasesUpdate />} />
      <Route path="delete" element={<PurchasesDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PurchasesRoutes;
