import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { RoleRoutingService } from '../services/role-routing.service';

/**
 * Redirige l'utente alla sua home in base al ruolo (DFP/PIAO -> area-privata-DFP, altrimenti area-privata-PA).
 * Replica il comportamento di IndexComponent passando `state: { fromIndex: true }` per area-privata-PA.
 * Da usare sul child `path: ''` del layout protetto.
 */
export const roleHomeRedirectGuard: CanActivateFn = () => {
  const router = inject(Router);
  const roleRoutingService = inject(RoleRoutingService);

  return roleRoutingService.getHomeRoute$().pipe(
    map((homeRoute) => {
      const extras =
        homeRoute === '/area-privata-PA' ? { state: { fromIndex: true } } : {};
      router.navigate([homeRoute], extras);
      return false;
    })
  );
};
