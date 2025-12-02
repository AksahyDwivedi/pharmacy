import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Customers from './customers';
import CustomersDetail from './customers-detail';
import CustomersUpdate from './customers-update';
import CustomersDeleteDialog from './customers-delete-dialog';

const CustomersRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Customers />} />
    <Route path="new" element={<CustomersUpdate />} />
    <Route path=":id">
      <Route index element={<CustomersDetail />} />
      <Route path="edit" element={<CustomersUpdate />} />
      <Route path="delete" element={<CustomersDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CustomersRoutes;
