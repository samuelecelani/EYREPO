import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { Sezione31DTO } from '../models/classes/sezione-31-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, Observable, of, Subject, tap } from 'rxjs';
import { CodPathEnum } from '../models/enums/cod-path.enum';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';
import { GraficoSezione31DTO } from '../models/classes/grafico-sezione-31-dto';

@Injectable({
  providedIn: 'root',
})
export class Sezione31Service {
  constructor(private http: HttpClient) {}

  private sessionStorageService = inject(SessionStorageService);

  // Subject per notificare quando la sezione31 viene aggiornata
  private sezione31Updated$ = new Subject<Sezione31DTO>();

  // Observable pubblico per sottoscriversi agli aggiornamenti
  public onSezione31Updated$ = this.sezione31Updated$.asObservable();

  save(sezione: Sezione31DTO) {
    return this.http.post<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_3_1}/save`),
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
      Path.url(`/${CodPathEnum.SEZIONE_3_1}/validazione/${idSection}`),
      {
        idSection: idSection,
      },
      { params }
    );
  }

  getSezione31ByIdPiao(idPiao: number): Observable<Sezione31DTO | undefined> {
    return this.http
      .get<
        GenericResponse<Sezione31DTO | undefined>
      >(Path.url(`/${CodPathEnum.SEZIONE_3_1}/${idPiao}`))
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  getGraficoSezione31(idPiao: number): Observable<GraficoSezione31DTO[]> {
    console.log(`[Sezione31Service] getGraficoSezione31 called with idPiao: ${idPiao}`);
    return this.http
      .get<GenericResponse<GraficoSezione31DTO[]>>(Path.url(`/${CodPathEnum.SEZIONE_3_1}/grafico`))
      .pipe(map((res) => res.data || []));
  }

  /**
   * Metodo centralizzato per ricaricare la sezione31 e aggiornare il PIAO in session
   * Recupera il PIAO dalla session, ricarica la sezione31 aggiornata dal backend
   * e aggiorna il PIAO in session con la nuova sezione31
   */
  reloadSezione31AndUpdateSession(): Observable<Sezione31DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    console.log('[Sezione31Service] reloadSezione31AndUpdateSession called', piaoDTO?.id);

    if (piaoDTO?.id) {
      return this.getSezione31ByIdPiao(piaoDTO.id).pipe(
        tap((sezione31) => {
          // Notifica che la sezione31 è stata aggiornata passando i dati
          if (sezione31) {
            this.sezione31Updated$.next(sezione31);
          }
        })
      );
    }

    console.log('[Sezione31Service] piaoDTO.id not found, returning undefined');
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
