import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { OAuthService } from 'angular-oauth2-oidc';
import { Path } from '../utils/path';
import { UserSessionService } from '../services/user-session-service.service';
import { AccountService } from '../services/account.service';
import { Session } from 'inspector/promises';
import { SessionStorageService } from '../services/session-storage.service';
import { KEY_PA_ATTIVA, KEY_USER } from '../utils/constants';

const NULL_OBJECT_ID = '';

/** Decodifica il payload di un JWT (base64url → JSON) */
function decodeJwtPayload(token: string): Record<string, any> | null {
  try {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
  } catch {
    return null;
  }
}

function getAuthTokenForEnvironment(oAuthService: OAuthService): string {
  const isLocal = ['localhost', '127.0.0.1', '::1'].includes(window.location.hostname);
  return isLocal ? oAuthService.getAccessToken() : oAuthService.getIdToken();
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const oAuthService = inject(OAuthService);
  const userSessionService = inject(UserSessionService);
  const sessionServiceStorage = inject(SessionStorageService);

  // Se il config non è ancora caricato o la richiesta non è verso il nostro backend
  if (!Path.baseUrl() || !req.url.startsWith(Path.baseUrl())) {
    return next(req);
  }

  let headers = req.headers;

  const token = getAuthTokenForEnvironment(oAuthService);
  if (token) {
    // Authorization: Bearer
    headers = headers.set('Authorization', 'Bearer ' + token);

    // X-User-Id: codice fiscale dall'access token (sempre aggiornato dopo il refresh)
    /*const claims = decodeJwtPayload(token);
    const codiceFiscale = claims?.['nickname'] ?? '';
    headers = headers.set("X-User-Id", codiceFiscale);*/
  }

  /*
    X-Amministrazione-Id: codePA della PA selezionata, altrimenti stringa vuota
    const pa = sessionServiceStorage.getItem(KEY_PA_ATTIVA);
    const paCode = pa?.codePA ?? NULL_OBJECT_ID;

  */
  headers = headers.set('X-Amministrazione-Id', NULL_OBJECT_ID);

  return next(req.clone({ headers }));
};
