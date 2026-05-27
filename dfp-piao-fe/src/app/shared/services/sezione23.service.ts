import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione23DTO } from '../models/classes/sezione-23-dto';
import { map, Observable, of, Subject, tap } from 'rxjs';
import { PIAODTO } from '../models/classes/piao-dto';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { CodPathEnum } from '../models/enums/cod-path.enum';

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
    return this.http.post<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_2_3}/save`),
      sezione
    );
  }

  validation(sezione23Id: number, testoSezione: string, campiModificati: string) {
    const codicePa = this.getCodicePaFromSession();
    let params = new HttpParams()
      .set('testoSezione', testoSezione)
      .set('campiModificati', campiModificati);
    if (codicePa) {
      params = params.set('codicePa', codicePa);
    }

    return this.http.patch<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_2_3}/validazione/${sezione23Id}`),
      {
        idSection: sezione23Id,
      },
      { params }
    );
  }

  getSezione23ByIdPiao(idPiao: number) {
    return this.http
      .get<GenericResponse<Sezione23DTO>>(Path.url(`/${CodPathEnum.SEZIONE_2_3}/${idPiao}`))
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  /**
   * Metodo centralizzato per ricaricare la sezione23 e aggiornare il PIAO in session
   * Recupera il PIAO dalla session, ricarica la sezione23 aggiornata dal backend
   * e aggiorna il PIAO in session con la nuova sezione23
   */
  reloadSezione23AndUpdateSession(): Observable<Sezione23DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    console.log('[Sezione23Service] reloadSezione23AndUpdateSession called', piaoDTO?.id);

    if (piaoDTO?.id) {
      return this.getSezione23ByIdPiao(piaoDTO.id).pipe(
        tap((sezione23) => {
          // Notifica che la sezione23 è stata aggiornata passando i dati
          if (sezione23) {
            this.sezione23Updated$.next(sezione23);
          }
        })
      );
    }

    console.log('[Sezione23Service] piaoDTO.id not found, returning undefined');
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
