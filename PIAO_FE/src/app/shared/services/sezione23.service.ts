import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione23DTO } from '../models/classes/sezione-23-dto';
import { map, Observable, of, Subject, tap } from 'rxjs';
import { PIAODTO } from '../models/classes/piao-dto';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';

@Injectable({
  providedIn: 'root',
})
export class Sezione23Service {
  constructor(private http: HttpClient) {}

  private sessionStorageService = inject(SessionStorageService);

  // Subject per notificare quando la sezione23 viene aggiornata
  private sezione23Updated$ = new Subject<Sezione23DTO>();

  // Observable pubblico per sottoscriversi agli aggiornamenti
  public onSezione23Updated$ = this.sezione23Updated$.asObservable();

  save(sezione: Sezione23DTO) {
    return this.http
      .post<GenericResponse<Sezione23DTO>>(Path.url('/sezione23/save'), sezione)
      .pipe(map((res) => res.data));
  }

  validation(sezione23Id: number) {
    return this.http.patch<GenericResponse<void>>(
      Path.url(`/sezione23/validazione/${sezione23Id}`),
      {
        idSection: sezione23Id,
      }
    );
  }

  getSezione23(piaoDTO: PIAODTO) {
    return this.http
      .post<GenericResponse<Sezione23DTO>>(Path.url(`/sezione23/piao`), piaoDTO)
      .pipe(map((res) => res.data));
  }

  /**
   * Metodo centralizzato per ricaricare la sezione22 e aggiornare il PIAO in session
   * Recupera il PIAO dalla session, ricarica la sezione22 aggiornata dal backend
   * e aggiorna il PIAO in session con la nuova sezione22
   */
  reloadSezione23AndUpdateSession(): Observable<Sezione23DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    console.log('[Sezione23Service] reloadSezione23AndUpdateSession called', piaoDTO?.id);

    if (piaoDTO?.id) {
      return this.getSezione23(piaoDTO).pipe(
        tap((sezione23) => {
          console.log('[Sezione23Service] Sezione23 reloaded, notifying subscribers', sezione23);
          piaoDTO.sezione23 = sezione23;
          this.sessionStorageService.setItem(KEY_PIAO, piaoDTO);
          // Notifica che la sezione23 è stata aggiornata passando i dati
          this.sezione23Updated$.next(sezione23);
        })
      );
    }

    console.log('[Sezione23Service] piaoDTO.id not found, returning undefined');
    return of(undefined);
  }
}
