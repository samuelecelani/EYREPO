
export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastOptions {
  duration?: number;
  id?: string;
  dismissible?: boolean;
}

export interface Toast {
  id: string;
  type: ToastType;
  message: string;
  createdAt: number;
  duration: number;
  dismissible: boolean;
}
