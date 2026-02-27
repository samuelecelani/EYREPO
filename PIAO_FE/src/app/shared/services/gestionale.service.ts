import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { UtenteRuoloPaDTO } from '../models/classes/utente-ruolo-pa-dto';

@Injectable({
  providedIn: 'root',
})
export class GestionaleService {
  constructor(private http: HttpClient) {}

  // GET /codice-pa/{codicePa}
  getUserByCodicePa(codicePa: string): Observable<UtenteRuoloPaDTO[]> {
    return this.http
      .get<GenericResponse<UtenteRuoloPaDTO[]>>(Path.url(`/tokenized/codice-pa/${codicePa}`))
      .pipe(map((res) => res.data ?? []));
  }

  // POST /utentepa
  saveUtentePa(utenteRuoloPa: UtenteRuoloPaDTO): Observable<UtenteRuoloPaDTO> {
    return this.http
      .post<GenericResponse<UtenteRuoloPaDTO>>(Path.url('/tokenized/utentepa'), utenteRuoloPa)
      .pipe(map((res) => res.data));
  }

  // DELETE /utentepa/{id}
  deleteUtentePa(id: number): Observable<void> {
    return this.http.delete<void>(Path.url(`/tokenized/utentepa/${id}`));
  }
}
