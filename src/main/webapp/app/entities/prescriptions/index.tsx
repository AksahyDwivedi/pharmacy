import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Prescriptions from './prescriptions';
import PrescriptionsDetail from './prescriptions-detail';
import PrescriptionsUpdate from './prescriptions-update';
import PrescriptionsDeleteDialog from './prescriptions-delete-dialog';

const PrescriptionsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Prescriptions />} />
    <Route path="new" element={<PrescriptionsUpdate />} />
    <Route path=":id">
      <Route index element={<PrescriptionsDetail />} />
      <Route path="edit" element={<PrescriptionsUpdate />} />
      <Route path="delete" element={<PrescriptionsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PrescriptionsRoutes;
