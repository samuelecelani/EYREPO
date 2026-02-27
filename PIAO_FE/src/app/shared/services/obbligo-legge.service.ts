import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, switchMap } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione23Service } from './sezione23.service';
import { ObbligoLeggeDTO } from '../models/classes/obbligo-legge-dto';
import { Sezione23DTO } from '../models/classes/sezione-23-dto';

@Injectable({
  providedIn: 'root',
})
export class ObbligoLeggeService {
  private sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  /**
   * Salva un nuovo obbligo di legge o aggiorna uno esistente
   * POST /api/v1/obbligo-legge/save
   */
  save(obbligo: any): Observable<any> {
    return this.http
      .post<GenericResponse<ObbligoLeggeDTO>>(Path.url('/obbligo-legge/save'), obbligo)
      .pipe(map((res) => res.data));
  }

  /**
   * Ottiene tutti gli obblighi di legge per una sezione 2.3
   * GET /api/v1/obbligo-legge/sezione23/{idSezione23}
   */
  getByIdSezione23(idSezione23: number): Observable<ObbligoLeggeDTO[]> {
    return this.http
      .get<GenericResponse<ObbligoLeggeDTO[]>>(Path.url(`/obbligo-legge/sezione23/${idSezione23}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Elimina un obbligo di legge
   * DELETE /api/v1/obbligo-legge/{id}
   */
  delete(id: number): Observable<Sezione23DTO | undefined> {
    return this.http
      .delete<GenericResponse<void>>(Path.url(`/obbligo-legge/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
