import { IMedicines } from 'app/shared/model/medicines.model';
import { ISales } from 'app/shared/model/sales.model';

export interface ISaleItems {
  id?: number;
  quantity?: number | null;
  price?: number | null;
  medicines?: IMedicines | null;
  sales?: ISales | null;
}

export const defaultValue: Readonly<ISaleItems> = {};
