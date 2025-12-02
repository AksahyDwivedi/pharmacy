import medicines from 'app/entities/medicines/medicines.reducer';
import medicineBatches from 'app/entities/medicine-batches/medicine-batches.reducer';
import customers from 'app/entities/customers/customers.reducer';
import suppliers from 'app/entities/suppliers/suppliers.reducer';
import purchases from 'app/entities/purchases/purchases.reducer';
import purchaseItems from 'app/entities/purchase-items/purchase-items.reducer';
import sales from 'app/entities/sales/sales.reducer';
import saleItems from 'app/entities/sale-items/sale-items.reducer';
import prescriptions from 'app/entities/prescriptions/prescriptions.reducer';
import payments from 'app/entities/payments/payments.reducer';
import supplierPayments from 'app/entities/supplier-payments/supplier-payments.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  medicines,
  medicineBatches,
  customers,
  suppliers,
  purchases,
  purchaseItems,
  sales,
  saleItems,
  prescriptions,
  payments,
  supplierPayments,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
