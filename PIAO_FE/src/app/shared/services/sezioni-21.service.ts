import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Sezione21DTO } from '../models/classes/sezione-21-dto';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, tap, of, Observable, Subject } from 'rxjs';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';

@Injectable({
  providedIn: 'root',
})
export class Sezione21Service {
  private sessionStorageService = inject(SessionStorageService);

  // Subject per notificare quando la sezione21 viene aggiornata
  private sezione21Updated$ = new Subject<Sezione21DTO>();

  // Observable pubblico per sottoscriversi agli aggiornamenti
  public onSezione21Updated$ = this.sezione21Updated$.asObservable();

  constructor(private http: HttpClient) {}

  save(sezione: Sezione21DTO) {
    return this.http.post<GenericResponse<void>>(Path.url('/sezione21/save'), sezione);
  }

  validation(idSection: number) {
    return this.http.patch<GenericResponse<void>>(Path.url(`/sezione21/validazione/${idSection}`), {
      idSection: idSection,
    });
  }

  getSezione21ByIdPiao(idPiao: number) {
    return this.http
      .get<GenericResponse<Sezione21DTO>>(Path.url(`/sezione21/${idPiao}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Metodo centralizzato per ricaricare la sezione21 e aggiornare il PIAO in session
   * Recupera il PIAO dalla session, ricarica la sezione21 aggiornata dal backend
   * e aggiorna il PIAO in session con la nuova sezione21
   */
  reloadSezione21AndUpdateSession(): Observable<Sezione21DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (piaoDTO) {
      return this.getSezione21ByIdPiao(piaoDTO.id || -1).pipe(
        tap((sezione21) => {
          // Notifica che la sezione21 è stata aggiornata passando i dati
          this.sezione21Updated$.next(sezione21);
        })
      );
    }

    return of(undefined);
  }
}
