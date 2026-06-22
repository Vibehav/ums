import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getUsers, createUser, updateUser, deleteUser, restoreUser, getDeletedUsers } from '../api/users';
import type { UserCreatePayload, UserUpdatePayload } from '../types/user';

export const useUsers = (page: number, size = 10, search?: string) =>
  useQuery({
    queryKey: ['users', page, size, search],
    queryFn: () => getUsers(page, size, search),
  });

export const useCreateUser = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: UserCreatePayload) => createUser(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['users'] }),
  });
};

export const useUpdateUser = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: UserUpdatePayload }) =>
      updateUser(id, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['users'] }),
  });
};

export const useDeleteUser = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deleteUser(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['users'] });
      qc.invalidateQueries({ queryKey: ['deleted-users'] });
    },
  });
};

export const useRestoreUser = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => restoreUser(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['users'] });
      qc.invalidateQueries({ queryKey: ['deleted-users'] });
    },
  });
};

export const useDeletedUsers = () =>
  useQuery({
    queryKey: ['deleted-users'],
    queryFn: getDeletedUsers,
  });
