import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { AuthService } from '../services/auth.service';

/**
 * Guard per proteggere le route che richiedono autenticazione.
 * Se l'utente non è autenticato, viene reindirizzato alla pagina di login.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const oAuthService = inject(OAuthService);
  const authService = inject(AuthService);

  // Verifica se l'utente ha un token valido
  if (oAuthService.hasValidAccessToken()) {
    return true; // Accesso consentito
  } else {
    // effettua il logout per pulire eventuali dati residui e redireziona al login
    authService.logout();
    return false; // Accesso negato, ma redirezionato al login
  }
};
