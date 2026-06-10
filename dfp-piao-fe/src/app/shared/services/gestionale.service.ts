import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, of } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { UtenteRuoloPaDTO } from '../models/classes/utente-ruolo-pa-dto';
import { LabelValue } from '../models/interfaces/label-value';

@Injectable({
  providedIn: 'root',
})
export class GestionaleService {
  constructor(private http: HttpClient) {}

  private ensureSuccess<T>(
    response: GenericResponse<T> | null | undefined,
    operation: string
  ): GenericResponse<T> {
    if (!response) {
      throw new Error(`${operation}: risposta vuota`);
    }

    if (response.status?.success !== true) {
      throw new Error(response.error?.messageError || `${operation}: operazione non riuscita`);
    }

    if (response.error?.messageError) {
      throw new Error(response.error.messageError);
    }

    return response;
  }

  private requireData<T>(
    response: GenericResponse<T> | null | undefined,
    operation: string
  ): T {
    const okResponse = this.ensureSuccess(response, operation);
    if (okResponse.data === null || okResponse.data === undefined) {
      throw new Error(`${operation}: payload di risposta non valido`);
    }
    return okResponse.data;
  }

  private getCodePAFromSessionStorage(): string | null {
    try {
      const paAttivaRaw = sessionStorage.getItem('paAttivaDTO');
      if (!paAttivaRaw) {
        return null;
      }

      const paAttiva = JSON.parse(paAttivaRaw);
      return typeof paAttiva?.codePA === 'string' && paAttiva.codePA ? paAttiva.codePA : null;
    } catch {
      return null;
    }
  }

  // GET /user
  getCurrentUser(): Observable<UtenteRuoloPaDTO> {
    return this.http
      .get<GenericResponse<UtenteRuoloPaDTO>>(Path.url('/tokenized/user'))
      .pipe(map((res) => this.requireData(res, 'Recupero utente corrente')));
  }

  // GET /tokenized/profile/{externalId}?codePA=...
  getProfileByExternalId(externalId: string): Observable<UtenteRuoloPaDTO> {
    let params = new HttpParams();
    const codePA = this.getCodePAFromSessionStorage();
    if (codePA) {
      params = params.set('codePA', codePA);
    }
    return this.http
      .get<
        GenericResponse<UtenteRuoloPaDTO>
      >(Path.url(`/tokenized/profile/${externalId}`), { params })
      .pipe(map((res) => this.requireData(res, 'Recupero dettaglio utente')));
  }

  // GET /codice-pa/{codicePa}
  getUserByCodicePa(codicePa: string): Observable<UtenteRuoloPaDTO[]> {
    return this.http
      .get<GenericResponse<UtenteRuoloPaDTO[]>>(Path.url(`/tokenized/codice-pa/${codicePa}`))
      .pipe(
        map((res) => {
          const okResponse = this.ensureSuccess(res, 'Recupero utenti per codice PA');
          return okResponse.data ?? [];
        })
      );
  }

  // POST /utentepa
  saveUtentePa(utenteRuoloPa: UtenteRuoloPaDTO): Observable<UtenteRuoloPaDTO> {
    return this.http
      .post<GenericResponse<UtenteRuoloPaDTO>>(Path.url('/tokenized/utentepa'), utenteRuoloPa)
      .pipe(map((res) => this.requireData(res, 'Creazione profilo utente')));
  }

  // PUT /utentepa/{id}?codicePa=...
  updateUtentePa(id: string, utenteRuoloPa: UtenteRuoloPaDTO): Observable<UtenteRuoloPaDTO> {
    const normalizedId = String(id ?? '').trim();
    if (!normalizedId) {
      throw new Error('Aggiornamento profilo utente: id utente mancante');
    }

    let params = new HttpParams();
    const codePA = this.getCodePAFromSessionStorage();
    if (codePA) {
      params = params.set('codicePa', codePA);
    }

    return this.http
      .put<GenericResponse<UtenteRuoloPaDTO>>(
        Path.url(`/tokenized/utentepa/${normalizedId}`),
        utenteRuoloPa,
        { params }
      )
      .pipe(map((res) => this.requireData(res, 'Aggiornamento profilo utente')));
  }

  // DELETE /utentepa/{id}?codicePa=...
  deleteUtentePa(id: string): Observable<void> {
    let params = new HttpParams();
    const codePA = this.getCodePAFromSessionStorage();
    if (codePA) {
      params = params.set('codicePa', codePA);
    }

    return this.http
      .delete<GenericResponse<void> | void>(Path.url(`/tokenized/utentepa/${id}`), { params })
      .pipe(
        map((res) => {
          if (res) {
            this.ensureSuccess(res, 'Revoca profilo utente');
          }
          return void 0;
        })
      );
  }

  // GET /ruoliByCodePA/{codicePa}
  getRolesByCodicePa(codicePa?: string): Observable<LabelValue[]> {
    const codePA = codicePa ?? this.getCodePAFromSessionStorage();
    if (!codePA) {
      return of([]);
    }

    return this.http
      .get<GenericResponse<LabelValue[]>>(Path.url(`/tokenized/ruoliByCodePA/${codePA}`))
      .pipe(
        map((res) => {
          const okResponse = this.ensureSuccess(res, 'Recupero ruoli per codice PA');
          return okResponse.data ?? [];
        })
      );
  }
}
