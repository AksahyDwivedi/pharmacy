import dayjs from 'dayjs';
import { ISuppliers } from 'app/shared/model/suppliers.model';

export interface IPurchases {
  id?: number;
  purchaseDate?: dayjs.Dayjs | null;
  invoiceNumber?: string | null;
  totalAmount?: number | null;
  suppliers?: ISuppliers | null;
}

export const defaultValue: Readonly<IPurchases> = {};
