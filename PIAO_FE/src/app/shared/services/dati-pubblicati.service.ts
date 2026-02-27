import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, switchMap } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione23Service } from './sezione23.service';
import { DatiPubblicatiDTO } from '../models/classes/dati-pubblicati-dto';
import { Sezione23DTO } from '../models/classes/sezione-23-dto';

@Injectable({
  providedIn: 'root',
})
export class DatiPubblicatiService {
  private sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  /**
   * Salva un nuovo dato pubblicato o aggiorna uno esistente
   * POST /api/v1/dati-pubblicati/save
   */
  save(datiPubblicati: DatiPubblicatiDTO): Observable<Sezione23DTO | undefined> {
    return this.http
      .post<GenericResponse<DatiPubblicatiDTO>>(Path.url('/dati-pubblicati/save'), datiPubblicati)
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  /**
   * Ottiene tutti i dati pubblicati per un obbligo di legge
   * GET /api/v1/dati-pubblicati/obbligo-legge/{obbligoLegge}
   */
  getByObbligoLegge(idObbligoLegge: number): Observable<DatiPubblicatiDTO[]> {
    return this.http
      .get<GenericResponse<DatiPubblicatiDTO[]>>(
        Path.url(`/dati-pubblicati/obbligo-legge/${idObbligoLegge}`)
      )
      .pipe(map((res) => res.data));
  }

  /**
   * Elimina un dato pubblicato
   * DELETE /api/v1/dati-pubblicati/{id}
   */
  delete(id: number): Observable<Sezione23DTO | undefined> {
    return this.http
      .delete<GenericResponse<void>>(Path.url(`/dati-pubblicati/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
