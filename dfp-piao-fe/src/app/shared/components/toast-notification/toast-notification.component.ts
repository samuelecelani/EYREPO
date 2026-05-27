// toast-container.component.ts
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription, timer } from 'rxjs';
import { SharedModule } from '../../module/shared/shared.module';
import { Toast, ToastType } from '../../models/interfaces/toast';
import { ToastService } from '../../services/toast.service';
import { SvgComponent } from '../svg/svg.component';

@Component({
  selector: 'piao-toast-notification',
  imports: [SharedModule, SvgComponent],
  templateUrl: './toast-notification.component.html',
  styleUrls: ['./toast-notification.component.scss'],
})
export class ToastNotificationComponent implements OnInit, OnDestroy {
  toasts: Toast[] = [];
  private sub?: Subscription;
  private timers = new Map<string, Subscription>();

  constructor(private toastService: ToastService) {}

  ngOnInit(): void {
    this.sub = this.toastService.toasts$.subscribe((toast) => {
      this.toasts.push(toast);
      // Auto-dismiss
      if (toast.duration > 0) {
        const t = timer(toast.duration).subscribe(() => this.dismiss(toast.id));
        this.timers.set(toast.id, t);
      }
    });
  }

  dismiss(id: string): void {
    const idx = this.toasts.findIndex((t) => t.id === id);
    if (idx >= 0) {
      const sub = this.timers.get(id);
      sub?.unsubscribe();
      this.timers.delete(id);
      this.toasts.splice(idx, 1);
    }
  }

  typeIcon(type: ToastType): string {
    switch (type) {
      case 'success':
        return 'CheckCircle';
      case 'error':
        return 'CloseCircle';
      case 'warning':
        return 'WarningCircle';
      default:
        return 'InfoCircle';
    }
  }

  typeColor(type: ToastType): string {
    switch (type) {
      case 'success':
        return '#008055';
      case 'error':
        return '#cc334d';
      case 'warning':
        return '#CC7A00';
      default:
        return '#003366';
    }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.timers.forEach((t) => t.unsubscribe());
    this.timers.clear();
  }
}
