import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SpinnerComponent } from './shared/components/spinner/spinner.component';
import { ToastNotificationComponent } from './shared/components/toast-notification/toast-notification.component';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, CommonModule, SpinnerComponent, ToastNotificationComponent],
    template: `
    <div class="min-h-screen flex flex-col">
      <main class="flex-1">
        <router-outlet />
      </main>
      <piao-toast-notification />
      <piao-spinner/>
    </div>
  `
})
export class AppComponent { }
