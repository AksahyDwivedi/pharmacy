import { IPurchases } from 'app/shared/model/purchases.model';
import { IMedicines } from 'app/shared/model/medicines.model';

export interface IPurchaseItems {
  id?: number;
  quantity?: number | null;
  price?: number | null;
  purchases?: IPurchases | null;
  medicines?: IMedicines | null;
}

export const defaultValue: Readonly<IPurchaseItems> = {};
