import dayjs from 'dayjs';
import { IPurchases } from 'app/shared/model/purchases.model';
import { IMedicines } from 'app/shared/model/medicines.model';

export interface IMedicineBatches {
  id?: number;
  batchNumber?: string | null;
  expiryDate?: dayjs.Dayjs | null;
  quantity?: number | null;
  purchases?: IPurchases | null;
  medicines?: IMedicines | null;
}

export const defaultValue: Readonly<IMedicineBatches> = {};
