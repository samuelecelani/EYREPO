import { inject } from '@angular/core';
import { CspNonceService } from '../services/csp-nonce.service';

export function loadCspNonce(): Promise<void> {
  const svc = inject(CspNonceService);

  return Promise.resolve().then(() => {
    // Se init() lancia, la Promise va in reject e lo intercettiamo nel provider
    svc.init();
  });
}
