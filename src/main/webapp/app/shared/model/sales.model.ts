import dayjs from 'dayjs';
import { ICustomers } from 'app/shared/model/customers.model';

export interface ISales {
  id?: number;
  saleDate?: dayjs.Dayjs | null;
  invoiceNumber?: string | null;
  totalAmount?: number | null;
  customers?: ICustomers | null;
}

export const defaultValue: Readonly<ISales> = {};
