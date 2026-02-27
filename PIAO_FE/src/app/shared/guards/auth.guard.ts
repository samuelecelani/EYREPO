import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AccountService } from '../services/account.service';
import { catchError, map, of } from 'rxjs';
import { LoginService } from '../services/login.service';

/**
 * Guard per proteggere le route che richiedono autenticazione.
 * Se l'utente non è autenticato, viene reindirizzato alla pagina di login.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const loginService = inject(LoginService);
  const accountService = inject(AccountService);

  const authGuardResponse = accountService.getAccount().pipe(
    map((user) => {
      if (user && user.paRiferimento) {
        const paAttiva = user.paRiferimento.filter((x) => x.attiva == true);
        if (paAttiva) {
          return true;
        }
        loginService.logout();
      }
      loginService.logout();
      return false;
    }),
    catchError(() => {
      loginService.login();
      return of(false);
    })
  );

  return authGuardResponse;
};
