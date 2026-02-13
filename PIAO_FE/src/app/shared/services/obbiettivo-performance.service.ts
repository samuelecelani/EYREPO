import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { switchMap } from 'rxjs';
import { ObbiettivoPerformanceDTO } from '../models/classes/obiettivo-performance-dto';
import { Sezione22Service } from './sezioni-22.service';

@Injectable({
  providedIn: 'root',
})
export class ObbiettivoPerformanceService {
  private sezione22Service = inject(Sezione22Service);

  constructor(private http: HttpClient) {}

  save(obiettivoDTO: ObbiettivoPerformanceDTO) {
    return this.http
      .post<
        GenericResponse<ObbiettivoPerformanceDTO>
      >(Path.url('/obbiettivo-performance/save'), obiettivoDTO)
      .pipe(switchMap(() => this.sezione22Service.reloadSezione22AndUpdateSession()));
  }

  delete(idObiettivo: number) {
    return this.http
      .delete<GenericResponse<void>>(Path.url(`/obbiettivo-performance/${idObiettivo}`))
      .pipe(switchMap(() => this.sezione22Service.reloadSezione22AndUpdateSession()));
  }
}
