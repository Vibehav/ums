// pre configured axios instance
import client from './client';
import type {
  User,
  UserCreatePayload,
  UserUpdatePayload,
  PagedResponse
} from '../types/user';

const BASE = '/api/v1/users';

// Get All
export const getUsers = async (page: number, size = 10, search?: string): Promise<PagedResponse<User>> => {
  const { data } = await client.get(BASE, { params: { page, size, sort: 'createdAt,desc', ...(search ? { search } : {}) } });
  return data;
};

// Get By Id
export const getUserById = async (id: number): Promise<User> => {
  const { data } = await client.get(`${BASE}/${id}`);
  return data;
};

// Create
export const createUser = async (payload: UserCreatePayload): Promise<User> => {
  const response = await client.post(
    `${BASE}`,
    payload
  );

  const data = response.data;

  return data;
};

// PATCH — backend uses @PatchMapping
export const updateUser = async (id: number, payload: UserUpdatePayload): Promise<User> => {
  const { data } = await client.patch(`${BASE}/${id}`, payload);
  return data;
};

// Delete
export const deleteUser = async (id: number): Promise<void> => {
  await client.delete(`${BASE}/${id}`);
};

export const restoreUser = async (id: number): Promise<void> => {
  await client.patch(`${BASE}/${id}/restore`);
};

export const getDeletedUsers = async (): Promise<User[]> => {
  const { data } = await client.get(`${BASE}/deleted`);
  return data;
};
