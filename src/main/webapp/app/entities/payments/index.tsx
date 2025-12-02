import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Payments from './payments';
import PaymentsDetail from './payments-detail';
import PaymentsUpdate from './payments-update';
import PaymentsDeleteDialog from './payments-delete-dialog';

const PaymentsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Payments />} />
    <Route path="new" element={<PaymentsUpdate />} />
    <Route path=":id">
      <Route index element={<PaymentsDetail />} />
      <Route path="edit" element={<PaymentsUpdate />} />
      <Route path="delete" element={<PaymentsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PaymentsRoutes;
