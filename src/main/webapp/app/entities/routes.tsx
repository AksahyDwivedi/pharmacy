import React from 'react';
import { Route } from 'react-router'; // eslint-disable-line

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Medicines from './medicines';
import MedicineBatches from './medicine-batches';
import Customers from './customers';
import Suppliers from './suppliers';
import Purchases from './purchases';
import PurchaseItems from './purchase-items';
import Sales from './sales';
import SaleItems from './sale-items';
import Prescriptions from './prescriptions';
import Payments from './payments';
import SupplierPayments from './supplier-payments';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="medicines/*" element={<Medicines />} />
        <Route path="medicine-batches/*" element={<MedicineBatches />} />
        <Route path="customers/*" element={<Customers />} />
        <Route path="suppliers/*" element={<Suppliers />} />
        <Route path="purchases/*" element={<Purchases />} />
        <Route path="purchase-items/*" element={<PurchaseItems />} />
        <Route path="sales/*" element={<Sales />} />
        <Route path="sale-items/*" element={<SaleItems />} />
        <Route path="prescriptions/*" element={<Prescriptions />} />
        <Route path="payments/*" element={<Payments />} />
        <Route path="supplier-payments/*" element={<SupplierPayments />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
