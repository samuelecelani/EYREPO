
// toast.service.ts
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Toast, ToastOptions, ToastType } from '../models/interfaces/toast';

@Injectable({ providedIn: 'root' })
export class ToastService {
  private toastSubject = new Subject<Toast>();
  public toasts$ = this.toastSubject.asObservable();

  success(message: string, options?: ToastOptions) {
    this.push(message, 'success', options);
  }
  error(message: string, options?: ToastOptions) {
    this.push(message, 'error', options);
  }
  warning(message: string, options?: ToastOptions) {
    this.push(message, 'warning', options);
  }
  info(message: string, options?: ToastOptions) {
    this.push(message, 'info', options);
  }

  private push(message: string, type: ToastType, options?: ToastOptions) {
    const id = options?.id ?? crypto.randomUUID?.() ?? Math.random().toString(36).slice(2);
    const toast: Toast = {
      id,
      type,
      message,
      createdAt: Date.now(),
      duration: options?.duration ?? 5000,
      dismissible: options?.dismissible ?? true,
    };
    this.toastSubject.next(toast);
  }
}
