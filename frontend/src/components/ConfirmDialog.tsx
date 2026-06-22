interface ConfirmDialogProps {
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmDialog({ message, onConfirm, onCancel }: ConfirmDialogProps) {
  return (
    <div className="overlay">
      <div className="dialog">
        <p>{message}</p>
        <div className="dialog__actions">
          <button className="btn btn--danger" onClick={onConfirm}>Delete</button>
          <button className="btn btn--secondary" onClick={onCancel}>Cancel</button>
        </div>
      </div>
    </div>
  );
}
