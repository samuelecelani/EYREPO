import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { Sezione331DTO } from '../models/classes/sezione-331-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, Observable, of, Subject, tap } from 'rxjs';
import { CodPathEnum } from '../models/enums/cod-path.enum';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';

@Injectable({
  providedIn: 'root',
})
export class Sezione331Service {
  constructor(private http: HttpClient) {}

  private sessionStorageService = inject(SessionStorageService);

  // Subject per notificare quando la sezione331 viene aggiornata
  private sezione331Updated$ = new Subject<Sezione331DTO>();

  // Observable pubblico per sottoscriversi agli aggiornamenti
  public onSezione331Updated$ = this.sezione331Updated$.asObservable();

  save(sezione: Sezione331DTO) {
    return this.http.post<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_3_3_1}/save`),
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
      Path.url(`/${CodPathEnum.SEZIONE_3_3_1}/validazione/${idSection}`),
      {
        idSection: idSection,
      },
      { params }
    );
  }

  getSezione331ByIdPiao(idPiao: number): Observable<Sezione331DTO | undefined> {
    return this.http
      .get<
        GenericResponse<Sezione331DTO | undefined>
      >(Path.url(`/${CodPathEnum.SEZIONE_3_3_1}/${idPiao}`))
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  /**
   * Metodo centralizzato per ricaricare la sezione23 e aggiornare il PIAO in session
   * Recupera il PIAO dalla session, ricarica la sezione23 aggiornata dal backend
   * e aggiorna il PIAO in session con la nuova sezione331
   */
  reloadSezione331AndUpdateSession(): Observable<Sezione331DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    console.log('[Sezione331Service] reloadSezione331AndUpdateSession called', piaoDTO?.id);

    if (piaoDTO?.id) {
      return this.getSezione331ByIdPiao(piaoDTO.id).pipe(
        tap((sezione331) => {
          // Notifica che la sezione331 è stata aggiornata passando i dati
          if (sezione331) {
            this.sezione331Updated$.next(sezione331);
          }
        })
      );
    }

    console.log('[Sezione331Service] piaoDTO.id not found, returning undefined');
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
