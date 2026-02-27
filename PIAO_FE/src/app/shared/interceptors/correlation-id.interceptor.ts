import { HttpInterceptorFn } from '@angular/common/http';
import { BYPASS_APP_INTERCEPTORS } from './interceptor.tokens';
//Interceptor per poter intercettare, tramite correlation-id, possibili log (error,debug...)
function generateCorrelationId(): string {
  const cryptoObj: Crypto | undefined = (globalThis as any).crypto;
  if (cryptoObj && typeof (cryptoObj as any).randomUUID === 'function') {
    return (cryptoObj as any).randomUUID();
  }
  // Fallback RFC4122 v4-like
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export const correlationIdInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(BYPASS_APP_INTERCEPTORS)) {
    return next(req);
  }
  const id = generateCorrelationId();
  const cloned = req.clone({
    setHeaders: {
      'x-correlation-id': id,
    },
  });
  return next(cloned);
};
