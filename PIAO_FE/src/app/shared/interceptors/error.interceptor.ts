import { HttpErrorResponse, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ToastService } from '../services/toast.service';
import { catchError, switchMap, throwError } from 'rxjs';
import { BYPASS_APP_INTERCEPTORS } from './interceptor.tokens';
import { LoginService } from '../services/login.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(BYPASS_APP_INTERCEPTORS)) {
    return next(req);
  }
  const toast = inject(ToastService);
  const login = inject(LoginService);
  return next(req).pipe(
    switchMap((event) => {
      // Gestisce risposte HTTP 200 che contengono un errore applicativo nel body
      if (event instanceof HttpResponse) {
        const body = event.body as any;
        if (body?.status?.success === false && body?.error?.messageError) {
          toast.error(body.error.messageError, { duration: 7000 });
          return throwError(
            () =>
              new HttpErrorResponse({
                error: body.error,
                status: 200,
                statusText: body.error.messageError,
                url: req.url,
              })
          );
        }
      }
      return [event];
    }),
    catchError((error: unknown) => {
      // Gestisce solo errori HTTP reali, non errori di parsing o altri errori
      if (error instanceof HttpErrorResponse) {
        if (error.status >= 400) {
          console.log(error);
          const backendMsg =
            (typeof error.error === 'string' ? error.error : error.error?.message) ||
            error.message ||
            error.statusText ||
            'Errore di rete';

          const message = `Errore ${error.status}: ${backendMsg}`;
          toast.error(message, { duration: 7000 });
        }
      }

      return throwError(() => error);
    })
  );
};
