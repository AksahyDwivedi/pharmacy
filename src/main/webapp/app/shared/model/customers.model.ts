export interface ICustomers {
  id?: number;
  name?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
}

export const defaultValue: Readonly<ICustomers> = {};
