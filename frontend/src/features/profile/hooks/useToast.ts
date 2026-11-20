import { useCallback, useEffect, useRef, useState } from 'react';
import type { ToastMessage, ToastType } from '../types/profile.types';

const DEFAULT_TOAST_DURATION_MS = 3200;

export interface UseToastResult {
  toasts: ToastMessage[];
  showToast: (type: ToastType, message: string, durationMs?: number) => void;
  dismissToast: (id: string) => void;
}

export function useToast(): UseToastResult {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);
  const timersRef = useRef<Map<string, number>>(new Map());

  const dismissToast = useCallback((id: string) => {
    const timer = timersRef.current.get(id);
    if (timer !== undefined) {
      window.clearTimeout(timer);
      timersRef.current.delete(id);
    }
    setToasts((prev) => prev.filter((item) => item.id !== id));
  }, []);

  const showToast = useCallback(
    (type: ToastType, message: string, durationMs = DEFAULT_TOAST_DURATION_MS) => {
      const id = `toast-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
      setToasts((prev) => [...prev, { id, type, message }]);

      const timer = window.setTimeout(() => {
        timersRef.current.delete(id);
        setToasts((prev) => prev.filter((item) => item.id !== id));
      }, durationMs);

      timersRef.current.set(id, timer);
    },
    []
  );

  useEffect(() => {
    const timers = timersRef.current;
    return () => {
      timers.forEach((timerId) => window.clearTimeout(timerId));
      timers.clear();
    };
  }, []);

  return {
    toasts,
    showToast,
    dismissToast,
  };
}
