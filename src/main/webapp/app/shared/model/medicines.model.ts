export interface IMedicines {
  id?: number;
  name?: string | null;
  manufacturer?: string | null;
  category?: string | null;
  price?: number | null;
  stock?: number | null;
}

export const defaultValue: Readonly<IMedicines> = {};
