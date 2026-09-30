import React, { createContext, useContext, useState, useCallback } from 'react';
import { X, CheckCircle2, AlertCircle, AlertTriangle, Info } from 'lucide-react';

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback((type, message, title, duration = 4000) => {
    const id = Math.random().toString(36).substring(2, 9);
    const newToast = { id, type, message, title, duration };
    setToasts((prev) => [...prev, newToast]);

    if (duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, duration);
    }
  }, [removeToast]);

  const toast = {
    success: (message, title) => addToast('success', message, title),
    error: (message, title) => addToast('error', message, title),
    warning: (message, title) => addToast('warning', message, title),
    info: (message, title) => addToast('info', message, title),
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="fixed bottom-5 right-5 z-50 flex flex-col space-y-3 max-w-md w-full pointer-events-none px-4">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={`pointer-events-auto flex items-start p-4 rounded-xl border backdrop-blur-md shadow-2xl transition-all duration-300 animate-slide-in ${
              t.type === 'success'
                ? 'bg-emerald-950/80 border-emerald-500/40 text-emerald-200'
                : t.type === 'error'
                ? 'bg-rose-950/80 border-rose-500/40 text-rose-200'
                : t.type === 'warning'
                ? 'bg-amber-950/80 border-amber-500/40 text-amber-200'
                : 'bg-obsidian-800/90 border-brand-indigo/40 text-indigo-200'
            }`}
          >
            <div className="flex-shrink-0 mt-0.5">
              {t.type === 'success' && <CheckCircle2 className="w-5 h-5 text-emerald-400" />}
              {t.type === 'error' && <AlertCircle className="w-5 h-5 text-rose-400" />}
              {t.type === 'warning' && <AlertTriangle className="w-5 h-5 text-amber-400" />}
              {t.type === 'info' && <Info className="w-5 h-5 text-indigo-400" />}
            </div>
            <div className="ml-3 flex-1">
              {t.title && <h4 className="text-sm font-semibold tracking-wide text-white">{t.title}</h4>}
              <p className="text-sm opacity-90 mt-0.5 leading-relaxed">{t.message}</p>
            </div>
            <button
              onClick={() => removeToast(t.id)}
              className="ml-3 inline-flex text-gray-400 hover:text-white transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}
