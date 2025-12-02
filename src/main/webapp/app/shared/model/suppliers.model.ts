export interface ISuppliers {
  id?: number;
  name?: string | null;
  contactPerson?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
}

export const defaultValue: Readonly<ISuppliers> = {};
