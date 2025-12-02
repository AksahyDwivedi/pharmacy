import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Medicines from './medicines';
import MedicinesDetail from './medicines-detail';
import MedicinesUpdate from './medicines-update';
import MedicinesDeleteDialog from './medicines-delete-dialog';

const MedicinesRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Medicines />} />
    <Route path="new" element={<MedicinesUpdate />} />
    <Route path=":id">
      <Route index element={<MedicinesDetail />} />
      <Route path="edit" element={<MedicinesUpdate />} />
      <Route path="delete" element={<MedicinesDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default MedicinesRoutes;
