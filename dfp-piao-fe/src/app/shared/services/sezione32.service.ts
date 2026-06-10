import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, of, Subject, tap } from 'rxjs';
import { Sezione32DTO } from '../models/classes/sezione-32-dto';
import { CodPathEnum } from '../models/enums/cod-path.enum';
import { GenericResponse } from '../models/interfaces/generic-response';
import { KEY_PIAO } from '../utils/constants';
import { Path } from '../utils/path';
import { PIAODTO } from '../models/classes/piao-dto';
import { SessionStorageService } from './session-storage.service';

@Injectable({
  providedIn: 'root',
})
export class Sezione32Service {
  constructor(private http: HttpClient) {}

  private sessionStorageService = inject(SessionStorageService);

  private sezione32Updated$ = new Subject<Sezione32DTO>();
  public onSezione32Updated$ = this.sezione32Updated$.asObservable();

  save(sezione: Sezione32DTO) {
    return this.http.post<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_3_2}/save`),
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
      Path.url(`/${CodPathEnum.SEZIONE_3_2}/validazione/${idSection}`),
      {
        idSection,
      },
      { params }
    );
  }

  getSezione32ByIdPiao(idPiao: number): Observable<Sezione32DTO | undefined> {
    return this.http
      .get<
        GenericResponse<Sezione32DTO | undefined>
      >(Path.url(`/${CodPathEnum.SEZIONE_3_2}/${idPiao}`))
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  reloadSezione32AndUpdateSession(): Observable<Sezione32DTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (piaoDTO?.id) {
      return this.getSezione32ByIdPiao(piaoDTO.id).pipe(
        tap((sezione32) => {
          if (sezione32) {
            this.sezione32Updated$.next(sezione32);
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
