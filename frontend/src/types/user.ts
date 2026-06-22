export interface User {
  id: number;
  name: string;
  email: string;
  primaryMobile: string;
  secondaryMobile?: string;
  aadhaar: string;
  pan: string;
  dateOfBirth: string;
  placeOfBirth: string;
  currentAddress: string;
  permanentAddress: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserCreatePayload {
  name: string;
  email: string;
  primaryMobile: string;
  secondaryMobile?: string;
  aadhaar: string;
  pan?: string;
  dateOfBirth: string;
  placeOfBirth?: string;
  currentAddress?: string;
  permanentAddress?: string;
}

export type UserUpdatePayload = Partial<UserCreatePayload>;

export interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}