import dayjs from 'dayjs';
import { ICustomers } from 'app/shared/model/customers.model';

export interface IPrescriptions {
  id?: number;
  doctorName?: string | null;
  prescriptionDate?: dayjs.Dayjs | null;
  notes?: string | null;
  customers?: ICustomers | null;
}

export const defaultValue: Readonly<IPrescriptions> = {};
