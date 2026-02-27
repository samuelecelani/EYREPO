
// spinner.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { SpinnerService } from '../services/spinner.service';

export const spinnerInterceptor: HttpInterceptorFn = (request, next) => {
  const spinnerService = inject(SpinnerService);

  // Ignora preflight o method non significativi
  if (request.method === 'OPTIONS') {
    return next(request);
  }

  // Header opzionale: se non presente, usa 'default'
  const spinnerId = request.headers.get('id-spinner') || 'default';

  // Se vuoi disattivarlo per certe call: usa 'none' in header
  if (spinnerId !== 'none') {
    spinnerService.addLoading(spinnerId);
  }

  return next(request).pipe(
    finalize(() => {
      if (spinnerId !== 'none') {
        spinnerService.removeLoading(spinnerId);
      }
    })
  );
};
