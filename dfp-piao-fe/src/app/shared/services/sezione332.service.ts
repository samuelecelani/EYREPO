import { AreaTematicaDTO } from './../models/classes/area-tematica-dto';
import { AmbitoCompetenzaDTO } from './../models/classes/ambito-competenza-dto';
import { TipologiaAttivitaDTO } from './../models/classes/tipologia-attivita-dto';
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { Sezione332DTO } from '../models/classes/sezione-332-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, Observable, of, shareReplay, Subject, tap } from 'rxjs';
import { CodPathEnum } from '../models/enums/cod-path.enum';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';
import { TipologiaDestinatariDTO } from '../models/classes/tipologia-destinatari-dto';

@Injectable({
  providedIn: 'root',
})
export class Sezione332Service {
  constructor(private http: HttpClient) {}

  private sessionStorageService = inject(SessionStorageService);

  private tipologiaAttivita$?: Observable<TipologiaAttivitaDTO[]>;
  private ambitoCompetenza$?: Observable<AmbitoCompetenzaDTO[]>;
  private areaTematica$?: Observable<AreaTematicaDTO[]>;
  private tipologiaDestinatari$?: Observable<TipologiaDestinatariDTO[]>;

  // Subject per notificare quando la sezione332 viene aggiornata
  private sezione332Updated$ = new Subject<Sezione332DTO>();

  // Observable pubblico per sottoscriversi agli aggiornamenti
  public onSezione332Updated$ = this.sezione332Updated$.asObservable();

  save(sezione: Sezione332DTO) {
    return this.http.post<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_3_3_2}/save`),
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
      Path.url(`/${CodPathEnum.SEZIONE_3_3_2}/validazione/${idSection}`),
      {
        idSection: idSection,
      },
      { params }
    );
  }

  getSezione332ByIdPiao(idPiao: number): Observable<Sezione332DTO | undefined> {
    return this.http
      .get<
        GenericResponse<Sezione332DTO | undefined>
      >(Path.url(`/${CodPathEnum.SEZIONE_3_3_2}/${idPiao}`))
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  /**
   * Metodo centralizzato per ricaricare la sezione332 e aggiornare il PIAO in session
   * Recupera il PIAO dalla session, ricarica la sezione332 aggiornata dal backend
   * e aggiorna il PIAO in session con la nuova sezione332
   */
  reloadSezione332AndUpdateSession(): Observable<Sezione332DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (piaoDTO?.id) {
      return this.getSezione332ByIdPiao(piaoDTO.id).pipe(
        tap((sezione332) => {
          // Notifica che la sezione332 è stata aggiornata passando i dati
          if (sezione332) {
            this.sezione332Updated$.next(sezione332);
          }
        })
      );
    }

    return of(undefined);
  }

  getTipologiaAttivita() {
    if (!this.tipologiaAttivita$) {
      this.tipologiaAttivita$ = this.http
        .get<
          GenericResponse<TipologiaAttivitaDTO[]>
        >(Path.url(`/${CodPathEnum.SEZIONE_3_3_2}/tipologia-attivita`))
        .pipe(
          map((res) => (res ? res.data : [])),
          shareReplay(1)
        );
    }
    return this.tipologiaAttivita$;
  }

  getAmbitoCompetenza() {
    if (!this.ambitoCompetenza$) {
      this.ambitoCompetenza$ = this.http
        .get<
          GenericResponse<AmbitoCompetenzaDTO[]>
        >(Path.url(`/${CodPathEnum.SEZIONE_3_3_2}/ambito-competenza`))
        .pipe(
          map((res) => (res ? res.data : [])),
          shareReplay(1)
        );
    }
    return this.ambitoCompetenza$;
  }

  getAreaTematica() {
    if (!this.areaTematica$) {
      this.areaTematica$ = this.http
        .get<
          GenericResponse<AreaTematicaDTO[]>
        >(Path.url(`/${CodPathEnum.SEZIONE_3_3_2}/area-tematica`))
        .pipe(
          map((res) => (res ? res.data : [])),
          shareReplay(1)
        );
    }
    return this.areaTematica$;
  }

  getTipologiaDestinatari() {
    if (!this.tipologiaDestinatari$) {
      this.tipologiaDestinatari$ = this.http
        .get<
          GenericResponse<TipologiaDestinatariDTO[]>
        >(Path.url(`/${CodPathEnum.SEZIONE_3_3_2}/tipologia-destinatari`))
        .pipe(
          map((res) => (res ? res.data : [])),
          shareReplay(1)
        );
    }
    return this.tipologiaDestinatari$;
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
