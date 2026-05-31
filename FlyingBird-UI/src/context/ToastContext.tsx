import React, { createContext, useContext, useState, useCallback, useRef } from 'react';
import { CheckCircle, XCircle, X } from 'lucide-react';

type ToastType = 'success' | 'error';

interface ToastItem {
  id: number;
  message: string;
  type: ToastType;
}

interface ToastContextType {
  showSuccess: (message: string) => void;
  showError: (message: string) => void;
}

const ToastContext = createContext<ToastContextType | null>(null);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastItem[]>([]);
  const idRef = useRef(0);

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    (message: string, type: ToastType) => {
      const id = ++idRef.current;
      setToasts((prev) => [...prev, { id, message, type }]);
      setTimeout(() => removeToast(id), 5000);
    },
    [removeToast]
  );

  const showSuccess = useCallback((msg: string) => addToast(msg, 'success'), [addToast]);
  const showError   = useCallback((msg: string) => addToast(msg, 'error'),   [addToast]);

  return (
    <ToastContext.Provider value={{ showSuccess, showError }}>
      {children}
      {toasts.length > 0 && (
        <div className="toast-container" role="region" aria-label="Notifications">
          {toasts.map((t) => (
            <div key={t.id} className={`toast toast--${t.type}`} role="alert">
              <span className="toast-icon">
                {t.type === 'success'
                  ? <CheckCircle size={16} />
                  : <XCircle size={16} />}
              </span>
              <span className="toast-message">{t.message}</span>
              <button
                className="toast-close"
                onClick={() => removeToast(t.id)}
                aria-label="Dismiss notification"
              >
                <X size={14} />
              </button>
            </div>
          ))}
        </div>
      )}
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextType => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
};
