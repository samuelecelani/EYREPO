import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Observable, switchMap } from 'rxjs';
import { Sezione22Service } from './sezioni-22.service';
import { Sezione22DTO } from '../models/classes/sezione-22-dto';
import { AdempimentoDTO } from '../models/classes/adempimento-dto';
import { TipologiaAdempimento } from '../models/enums/tipologia-adempimento.enum';

@Injectable({
  providedIn: 'root',
})
export class AdempimentiService {
  private sezione22Service = inject(Sezione22Service);

  constructor(private http: HttpClient) {}

  deleteAdempimento(
    id: number,
    idPiao: number,
    tipologia: TipologiaAdempimento,
    testoSezione: string
  ): Observable<Sezione22DTO | undefined> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'adempimento.' + tipologia.toString())
      .set('testoSezione', testoSezione);
    return this.http
      .delete<GenericResponse<void>>(Path.url(`/adempimento/${id}`), { params })
      .pipe(switchMap(() => this.sezione22Service.reloadSezione22AndUpdateSession()));
  }

  saveOrUpdateAdempimento(adempimento: AdempimentoDTO): Observable<Sezione22DTO | undefined> {
    return this.http
      .post<GenericResponse<void>>(Path.url('/adempimento'), adempimento)
      .pipe(switchMap(() => this.sezione22Service.reloadSezione22AndUpdateSession()));
  }
}
