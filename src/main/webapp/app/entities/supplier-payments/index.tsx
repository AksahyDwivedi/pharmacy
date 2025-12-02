import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import SupplierPayments from './supplier-payments';
import SupplierPaymentsDetail from './supplier-payments-detail';
import SupplierPaymentsUpdate from './supplier-payments-update';
import SupplierPaymentsDeleteDialog from './supplier-payments-delete-dialog';

const SupplierPaymentsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<SupplierPayments />} />
    <Route path="new" element={<SupplierPaymentsUpdate />} />
    <Route path=":id">
      <Route index element={<SupplierPaymentsDetail />} />
      <Route path="edit" element={<SupplierPaymentsUpdate />} />
      <Route path="delete" element={<SupplierPaymentsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SupplierPaymentsRoutes;
