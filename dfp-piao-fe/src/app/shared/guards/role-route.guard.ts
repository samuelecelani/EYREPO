import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AccountService } from '../services/account.service';
import { RoleRoutingService } from '../services/role-routing.service';

const COMMON_ALLOWED_EXACT = new Set(['notifiche']);

const PA_ALLOWED_EXACT = new Set([
  ...COMMON_ALLOWED_EXACT,
  'area-privata-PA',
  'area-privata-PA/mancata-compilazione',
  'profilo',
  'servizi-piao',
  'validazione',
  'novita',
  'gestionale',
  'help-desk',
]);

const DFP_ALLOWED_EXACT = new Set([
  ...COMMON_ALLOWED_EXACT,
  'area-privata-DFP',
  'profilo',
  'cruscotti-di-analisi',
  'documenti',
  'avvisi',
  'gestionale',
  'gestione-piao',
  'storico-dichiarazioni',
  'solleciti',
]);

const PA_ALLOWED_PREFIXES = ['servizi-piao', 'gestionale/gestione-profilo-utente'];
const DFP_ALLOWED_PREFIXES = [
  'gestione-piao',
  'servizi-piao',
  'avvisi',
  'solleciti',
  'storico-dichiarazioni',
  'gestionale/gestione-profilo-utente',
];

function normalizePagesPath(url: string): string {
  const cleanUrl = url.split(/[?#]/)[0] || '';
  return cleanUrl.replace(/^\/+/, '').replace(/\/+$/, '');
}

function isAllowedByRole(path: string, isDfp: boolean): boolean {
  if (!path) {
    return true;
  }

  const exactAllowed = isDfp ? DFP_ALLOWED_EXACT : PA_ALLOWED_EXACT;
  if (exactAllowed.has(path)) {
    return true;
  }

  const prefixes = isDfp ? DFP_ALLOWED_PREFIXES : PA_ALLOWED_PREFIXES;
  return prefixes.some((prefix) => path.startsWith(`${prefix}/`));
}

export const roleRouteGuard: CanActivateChildFn = (route, state) => {
  const router = inject(Router);
  const accountService = inject(AccountService);
  const roleRoutingService = inject(RoleRoutingService);

  const requestedPath = normalizePagesPath(state.url);

  return accountService.getAccount().pipe(
    map((user) => {
      const isDfp = roleRoutingService.isDfpAuthority(user?.typeAuthority);
      const homeRoute = roleRoutingService.getHomeRouteByAuthority(user?.typeAuthority);

      if (isAllowedByRole(requestedPath, isDfp)) {
        return true;
      }

      return router.parseUrl(homeRoute);
    }),
    catchError(() => of(router.parseUrl('/')))
  );
};
