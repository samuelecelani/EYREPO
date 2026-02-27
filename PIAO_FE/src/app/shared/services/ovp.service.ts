import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, switchMap } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { OVPDTO } from '../models/classes/ovp-dto';
import { Sezione21Service } from './sezioni-21.service';

@Injectable({
  providedIn: 'root',
})
export class OvpService {
  constructor(private http: HttpClient) {}
  private sezione21Service = inject(Sezione21Service);

  /**
   * Salva un nuovo OVP o aggiorna uno esistente
   * POST /ovp/save
   */
  save(ovp: OVPDTO) {
    return this.http
      .post<GenericResponse<void>>(Path.url('/ovp/save'), ovp)
      .pipe(switchMap(() => this.sezione21Service.reloadSezione21AndUpdateSession()));
  }

  /**
   * Elimina un OVP
   * DELETE /ovp/{id}
   */
  delete(id: number) {
    return this.http.delete<GenericResponse<void>>(Path.url(`/ovp/${id}`)).pipe(
      map((res) => res.data),
      switchMap(() => this.sezione21Service.reloadSezione21AndUpdateSession())
    );
  }

  /**
   * Ottiene un OVP per ID
   * GET /ovp/{id}
   */
  getById(id: number) {
    return this.http
      .get<GenericResponse<OVPDTO>>(Path.url(`/ovp/${id}`))
      .pipe(map((res) => res.data));
  }

  getAllOvpByIdPiao(idPiao: number) {
    return this.http
      .get<GenericResponse<OVPDTO[]>>(Path.url(`/ovp/piao/${idPiao}`))
      .pipe(map((res) => res.data));
  }
}
