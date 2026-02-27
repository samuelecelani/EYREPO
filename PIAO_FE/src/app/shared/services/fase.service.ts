import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Observable, map, switchMap } from 'rxjs';
import { Sezione22Service } from './sezioni-22.service';
import { Sezione22DTO } from '../models/classes/sezione-22-dto';
import { FaseDTO } from '../models/classes/fase-dto';

@Injectable({
  providedIn: 'root',
})
export class FaseService {
  private sezione22Service = inject(Sezione22Service);

  constructor(private http: HttpClient) {}

  /**
   * Salva o aggiorna una fase
   * POST /fase
   */
  save(fase: FaseDTO): Observable<Sezione22DTO | undefined> {
    return this.http
      .post<GenericResponse<void>>(Path.url('/fase'), fase)
      .pipe(switchMap(() => this.sezione22Service.reloadSezione22AndUpdateSession()));
  }

  deleteFase(id: number): Observable<Sezione22DTO | undefined> {
    return this.http
      .delete<GenericResponse<void>>(Path.url(`/fase/${id}`))
      .pipe(switchMap(() => this.sezione22Service.reloadSezione22AndUpdateSession()));
  }
}
