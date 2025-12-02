import dayjs from 'dayjs';
import { ISuppliers } from 'app/shared/model/suppliers.model';
import { IPurchases } from 'app/shared/model/purchases.model';

export interface ISupplierPayments {
  id?: number;
  paymentDate?: dayjs.Dayjs | null;
  paymentMethod?: string | null;
  paymentStatus?: string | null;
  amountPaid?: number | null;
  suppliers?: ISuppliers | null;
  purchases?: IPurchases | null;
}

export const defaultValue: Readonly<ISupplierPayments> = {};
