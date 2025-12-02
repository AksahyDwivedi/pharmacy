import dayjs from 'dayjs';
import { ISales } from 'app/shared/model/sales.model';

export interface IPayments {
  id?: number;
  paymentDate?: dayjs.Dayjs | null;
  paymentMethod?: string | null;
  paymentStatus?: string | null;
  amount?: number | null;
  sales?: ISales | null;
}

export const defaultValue: Readonly<IPayments> = {};
