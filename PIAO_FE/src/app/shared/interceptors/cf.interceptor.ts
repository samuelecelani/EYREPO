import { HttpInterceptorFn } from '@angular/common/http';

export const cfInterceptor: HttpInterceptorFn = (request, next) => {
  const copiedReq = request.clone({
    setHeaders: {
      'X-Fiscal-Code': 'TESTTEST',
    },
  });

  return next(copiedReq);
};
