import { ToastService } from './../services/toast.service';
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

/**
 * Guard per proteggere le rotte dei Servizi PIAO.
 * Verifica che piaoDTO sia presente nel SessionStorage prima di permettere l'accesso
 * alle rotte successive nello scope dei servizi PIAO.
 *
 * Se piaoDTO non è presente, l'utente viene reindirizzato alla pagina principale dei servizi PIAO.
 */
export const piaoSessionStorageGuard: CanActivateFn = (route, state) => {
  // Iniezione del Router per eseguire il redirect se la guardia fallisce
  const router = inject(Router);
  const toastService = inject(ToastService);

  // Recupera piaoDTO dal SessionStorage
  // SessionStorage contiene dati temporanei validi per la sessione corrente
  const piaoDTO = sessionStorage.getItem('piaoDTO');

  // Verifica se piaoDTO esiste nel SessionStorage
  if (piaoDTO) {
    // Se piaoDTO è presente, l'utente ha i permessi per accedere a questa rotta
    return true;
  } else {
    // Se piaoDTO non è presente, l'utente non ha accesso
    // Reindirizza alla pagina principale dei servizi PIAO
    toastService.warning('Accesso negato: piaoDTO non presente nel SessionStorage.');
    router.navigate(['/pages/area-privata-PA']);
    return false;
  }
};
