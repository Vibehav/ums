import type { ReactNode } from 'react';

interface DrawerProps {
  isOpen: boolean;
  title: string;
  onClose: () => void;
  children: ReactNode;
}

export default function Drawer({ isOpen, title, onClose, children }: DrawerProps) {
  if (!isOpen) return null;

  return (
    <>
      <div className="overlay" onClick={onClose} />
      <div className="drawer">
        <div className="drawer__header">
          <h2 className="drawer__title">{title}</h2>
          <button className="drawer__close" onClick={onClose}>×</button>
        </div>
        <div className="drawer__body">{children}</div>
      </div>
    </>
  );
}
