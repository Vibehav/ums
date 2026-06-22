import { useState, useCallback } from 'react';
import { useUsers, useCreateUser, useUpdateUser, useDeleteUser, useRestoreUser, useDeletedUsers } from './hooks/useUsers';
import UsersTable from './components/UsersTable';
import Drawer from './components/Drawer';
import UserForm from './components/UserForm';
import Pagination from './components/Pagination';
import ConfirmDialog from './components/ConfirmDialog';
import Toast from './components/Toast';
import DeletedUsersTable from './components/DeletedUsersTable';
import type { User, UserCreatePayload } from './types/user';


interface ToastState {
  message: string;
  type: 'success' | 'error';
}

export default function App() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [deletingUser, setDeletingUser] = useState<User | null>(null);
  const [toast, setToast] = useState<ToastState | null>(null);

  const { data, isLoading, isError } = useUsers(page, 10, search || undefined);
  const createMutation = useCreateUser();
  const updateMutation = useUpdateUser();
  const deleteMutation = useDeleteUser();
  const restoreMutation = useRestoreUser();
  const { data: deletedUsers = [] } = useDeletedUsers();

  const showToast = useCallback((message: string, type: 'success' | 'error') => {
    setToast({ message, type });
  }, []);

  const openCreate = () => { setEditingUser(null); setDrawerOpen(true); };
  const openEdit = (user: User) => { setEditingUser(user); setDrawerOpen(true); };
  const closeDrawer = () => { setDrawerOpen(false); setEditingUser(null); };

  const handleSubmit = async (payload: UserCreatePayload) => {
    try {
      if (editingUser) {
        await updateMutation.mutateAsync({ id: editingUser.id, payload });
        showToast('User updated successfully', 'success');
      } else {
        await createMutation.mutateAsync(payload);
        showToast('User created successfully', 'success');
      }
      closeDrawer();
    } catch {
      showToast('Something went wrong. Please try again.', 'error');
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingUser) return;
    try {
      await deleteMutation.mutateAsync(deletingUser.id);
      showToast('User deleted successfully', 'success');
    } catch {
      showToast('Failed to delete user.', 'error');
    } finally {
      setDeletingUser(null);
    }
  };

  const handleRestore = async (user: User) => {
    try {
      await restoreMutation.mutateAsync(user.id);
      showToast('User restored successfully', 'success');
    } catch {
      showToast('Failed to restore user.', 'error');
    }
  };

  const isSaving = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="app">
      <header className="header">
        <h1 className="header__title">User Management</h1>
        <button className="btn btn--primary" onClick={openCreate}>+ Add User</button>
      </header>

      <main className="main">
        <div className="toolbar">
          <input
            className="search-input"
            type="text"
            placeholder="Search by name or email..."
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
          />
          {data && <span className="result-count">{data.totalElements} user{data.totalElements !== 1 ? 's' : ''}</span>}
        </div>

        {isLoading && <p className="status">Loading...</p>}
        {isError && <p className="status status--error">Failed to load users. Is the backend running at localhost:8080?</p>}

        {data && (
          <>
            <UsersTable
              users={data.content}
              onEdit={openEdit}
              onDelete={setDeletingUser}
            />
            <Pagination
              page={page}
              totalPages={data.totalPages}
              onPageChange={setPage}
            />
            <DeletedUsersTable
              users={deletedUsers}
              onRestore={handleRestore}
            />
          </>
        )}
      </main>

      <Drawer isOpen={drawerOpen} title={editingUser ? 'Edit User' : 'Add User'} onClose={closeDrawer}>
        <UserForm user={editingUser} onSubmit={handleSubmit} isLoading={isSaving} />
      </Drawer>

      {deletingUser && (
        <ConfirmDialog
          message={`Delete "${deletingUser.name}"? This cannot be undone.`}
          onConfirm={handleDeleteConfirm}
          onCancel={() => setDeletingUser(null)}
        />
      )}

      {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}
    </div>
  );
}
