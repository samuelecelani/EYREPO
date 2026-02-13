import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PIAODTO } from '../models/classes/piao-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { map, Observable, tap } from 'rxjs';
import { StrutturaIndicePiaoDTO } from '../models/classes/struttura-indice-piao-dto';

@Injectable({
  providedIn: 'root',
})
export class PIAOService {
  constructor(private http: HttpClient) {}

  getOrCreatePiao(piaoDTO: PIAODTO): Observable<PIAODTO> {
    return this.http
      .post<GenericResponse<PIAODTO>>(Path.url('/piao/initialize'), piaoDTO)
      .pipe(map((res) => res.data));
  }

  redigiPiaoIsAllowed(codPA: string): Observable<boolean> {
    const params = new HttpParams().set('codPAFK', codPA);

    return this.http
      .get<GenericResponse<boolean>>(Path.url('/piao/redigi/allowed'), {
        params,
      })
      .pipe(map((res) => res.data));
  }

  getStructureIndicePIAO(idPiao?: number): Observable<StrutturaIndicePiaoDTO[]> {
    const params = idPiao ? new HttpParams().set('idPiao', idPiao) : {};
    return this.http
      .get<GenericResponse<StrutturaIndicePiaoDTO[]>>(Path.url('/struttura/piao'), {
        params,
      })
      .pipe(
        map((res) => res.data),
        tap((res) => {
          res.forEach((x) => {
            x.children.forEach((y) => {
              y.numeroSezione = y.numeroSezione.split('').join('.');
            });
          });
        })
      );
  }

  getAllPiao(codPA: string): Observable<PIAODTO[]> {
    const params = new HttpParams().set('codPAFK', codPA);

    return this.http
      .get<GenericResponse<PIAODTO[]>>(Path.url('/piao/findAllPiao'), {
        params,
      })
      .pipe(map((res) => res.data));
  }
}
