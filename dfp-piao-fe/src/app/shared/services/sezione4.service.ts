import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione4DTO } from '../models/classes/sezione-4-dto';
import { map, Observable, of, Subject, tap } from 'rxjs';
import { PIAODTO } from '../models/classes/piao-dto';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { CodPathEnum } from '../models/enums/cod-path.enum';

@Injectable({
  providedIn: 'root',
})
export class Sezione4Service {
  constructor(private http: HttpClient) {}

  private sessionStorageService = inject(SessionStorageService);

  // Subject per notificare quando la sezione4 viene aggiornata
  private sezione4Updated$ = new Subject<Sezione4DTO>();

  // Observable pubblico per sottoscriversi agli aggiornamenti
  public onSezione4Updated$ = this.sezione4Updated$.asObservable();

  save(sezione: Sezione4DTO) {
    return this.http.post<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_4}/save`),
      sezione
    );
  }

  validation(sezione4Id: number, testoSezione: string, campiModificati: string) {
    const codicePa = this.getCodicePaFromSession();
    let params = new HttpParams()
      .set('testoSezione', testoSezione)
      .set('campiModificati', campiModificati);
    if (codicePa) {
      params = params.set('codicePa', codicePa);
    }

    return this.http.patch<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_4}/validazione/${sezione4Id}`),
      {
        idSection: sezione4Id,
      },
      { params }
    );
  }

  getSezione4ByIdPiao(idPiao: number) {
    return this.http
      .get<
        GenericResponse<Sezione4DTO | undefined>
      >(Path.url(`/${CodPathEnum.SEZIONE_4}/${idPiao}`))
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  /**
   * Metodo centralizzato per ricaricare la sezione4 e aggiornare il PIAO in session
   * Recupera il PIAO dalla session, ricarica la sezione4 aggiornata dal backend
   * e aggiorna il PIAO in session con la nuova sezione4
   */
  reloadSezione4AndUpdateSession(): Observable<Sezione4DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);


    if (piaoDTO?.id) {
      return this.getSezione4ByIdPiao(piaoDTO.id).pipe(
        tap((sezione4) => {
          // Notifica che la sezione4 è stata aggiornata passando i dati
          if (sezione4) {
            this.sezione4Updated$.next(sezione4);
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
