import { HttpInterceptorFn } from "@angular/common/http";

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const copiedReq = request.clone({
    withCredentials: true
  });

  return next(copiedReq);
}
