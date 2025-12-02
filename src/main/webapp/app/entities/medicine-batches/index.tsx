import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import MedicineBatches from './medicine-batches';
import MedicineBatchesDetail from './medicine-batches-detail';
import MedicineBatchesUpdate from './medicine-batches-update';
import MedicineBatchesDeleteDialog from './medicine-batches-delete-dialog';

const MedicineBatchesRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<MedicineBatches />} />
    <Route path="new" element={<MedicineBatchesUpdate />} />
    <Route path=":id">
      <Route index element={<MedicineBatchesDetail />} />
      <Route path="edit" element={<MedicineBatchesUpdate />} />
      <Route path="delete" element={<MedicineBatchesDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default MedicineBatchesRoutes;
