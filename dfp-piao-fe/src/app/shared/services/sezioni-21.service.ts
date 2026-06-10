import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Sezione21DTO } from '../models/classes/sezione-21-dto';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, tap, of, Observable, Subject } from 'rxjs';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';
import { CodPathEnum } from '../models/enums/cod-path.enum';

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
    return this.http.post<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_2_1}/save`),
      sezione
    );
  }

  validation(idSection: number, testoSezione: string, campiModificati: string) {
    const codicePa = this.getCodicePaFromSession();
    let params = new HttpParams()
      .set('testoSezione', testoSezione)
      .set('campiModificati', campiModificati);
    if (codicePa) {
      params = params.set('codicePa', codicePa);
    }
    return this.http.patch<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_2_1}/validazione/${idSection}`),
      {
        idSection: idSection,
      },
      { params }
    );
  }

  getSezione21ByIdPiao(idPiao: number) {
    return this.http
      .get<GenericResponse<Sezione21DTO>>(Path.url(`/${CodPathEnum.SEZIONE_2_1}/${idPiao}`))
      .pipe(map((res) => (res ? res.data : undefined)));
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
          if (sezione21) {
            // Notifica che la sezione21 è stata aggiornata passando i dati
            this.sezione21Updated$.next(sezione21);
          }
        })
      );
    }

    return of(undefined);
  }

  private getCodicePaFromSession(): string | null {
    try {
      const paAttivaRaw = sessionStorage.getItem('paAttivaDTO');
      if (!paAttivaRaw) return null;
      const paAttiva = JSON.parse(paAttivaRaw);
      return typeof paAttiva?.codePA === 'string' && paAttiva.codePA ? paAttiva.codePA : null;
    } catch {
      return null;
    }
  }
}
