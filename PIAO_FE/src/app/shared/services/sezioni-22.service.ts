import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Sezione22DTO } from '../models/classes/sezione-22-dto';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, tap, of, Observable, Subject } from 'rxjs';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';

@Injectable({
  providedIn: 'root',
})
export class Sezione22Service {
  private sessionStorageService = inject(SessionStorageService);

  // Subject per notificare quando la sezione22 viene aggiornata
  private sezione22Updated$ = new Subject<Sezione22DTO>();

  // Observable pubblico per sottoscriversi agli aggiornamenti
  public onSezione22Updated$ = this.sezione22Updated$.asObservable();

  constructor(private http: HttpClient) {}

  save(sezione: Sezione22DTO) {
    return this.http.post<GenericResponse<void>>(Path.url('/sezione22/save'), sezione);
  }

  validation(idSection: number) {
    return this.http.patch<GenericResponse<void>>(Path.url(`/sezione22/validazione/${idSection}`), {
      idSection: idSection,
    });
  }

  getSezione22ByIdPiao(idPiao: number) {
    return this.http
      .get<GenericResponse<Sezione22DTO>>(Path.url(`/sezione22/${idPiao}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Metodo centralizzato per ricaricare la sezione22 e aggiornare il PIAO in session
   * Recupera il PIAO dalla session, ricarica la sezione22 aggiornata dal backend
   * e aggiorna il PIAO in session con la nuova sezione22
   */
  reloadSezione22AndUpdateSession(): Observable<Sezione22DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    console.log('[Sezione22Service] reloadSezione22AndUpdateSession called', piaoDTO?.id);

    if (piaoDTO?.id) {
      return this.getSezione22ByIdPiao(piaoDTO.id).pipe(
        tap((sezione22) => {
          // Notifica che la sezione22 è stata aggiornata passando i dati
          this.sezione22Updated$.next(sezione22);
        })
      );
    }

    console.log('[Sezione22Service] piaoDTO.id not found, returning undefined');
    return of(undefined);
  }
}
