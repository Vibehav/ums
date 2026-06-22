import type { User } from '../types/user';

interface DeletedUsersTableProps {
    users: User[];
    onRestore: (user: User) => void;
}

export default function DeletedUsersTable({
    users,
    onRestore,
}: DeletedUsersTableProps) {

    if (users.length === 0) {
        return <p className="empty">No deleted users.</p>;
    }

    return (
        <div className="table-wrapper">
            <h2>Deleted Users</h2>

            <table className="table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Primary Mobile</th>
                        <th>Aadhaar</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.name}</td>
                            <td>{user.email}</td>
                            <td>{user.primaryMobile}</td>
                            <td>{user.aadhaar}</td>

                            <td>
                                <button
                                    className="btn btn--sm btn--secondary"
                                    onClick={() => onRestore(user)}
                                >
                                    Restore
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}