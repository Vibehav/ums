import type { User } from '../types/user';

interface UsersTableProps {
  users: User[];
  onEdit: (user: User) => void;
  onDelete: (user: User) => void;
}

export default function UsersTable({
  users,
  onEdit,
  onDelete,
}: UsersTableProps) {
  if (users.length === 0) {
    return <p className="empty">No users found. Add one to get started.</p>;
  }

  return (
    <div className="table-wrapper">
      <table className="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Primary Mobile</th>
            <th>Secondary Mobile</th>
            <th>Aadhaar</th>
            <th>PAN</th>
            <th>Date of Birth</th>
            <th>Place of Birth</th>
            <th>Current Address</th>
            <th>Permanent Address</th>
            <th>Actions</th>
          </tr>
        </thead>

        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.name}</td>
              <td>{user.email}</td>
              <td>{user.primaryMobile}</td>

              <td>{user.secondaryMobile || '—'}</td>
              <td>{user.aadhaar}</td>
              <td>{user.pan || '—'}</td>

              <td>{user.dateOfBirth}</td>

              <td>{user.placeOfBirth || '—'}</td>
              <td>{user.currentAddress || '—'}</td>
              <td>{user.permanentAddress || '—'}</td>

              <td className="table__actions">
                <button
                  className="btn btn--sm btn--secondary"
                  onClick={() => onEdit(user)}
                >
                  Edit
                </button>

                <button
                  className="btn btn--sm btn--danger"
                  onClick={() => onDelete(user)}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}