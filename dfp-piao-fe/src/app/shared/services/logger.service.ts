import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoggerService {
  info(message: string, ...args: unknown[]) {
    // eslint-disable-next-line no-console
  }
  error(message: string, ...args: unknown[]) {
    // eslint-disable-next-line no-console
    console.error('[ERROR]', message, ...args);
  }
}
