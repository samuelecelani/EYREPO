import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Sezione1DTO } from '../models/classes/sezione-1-dto';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, Observable } from 'rxjs';
import { ItemMatriceDTO } from '../models/classes/item-matrice-dto';

@Injectable({
  providedIn: 'root',
})
export class Sezione1Service {
  constructor(private http: HttpClient) {}

  save(sezione: Sezione1DTO) {
    return this.http.post<GenericResponse<void>>(Path.url('/sezione1/save'), sezione);
  }

  validation(idSection: number) {
    return this.http.patch<GenericResponse<void>>(Path.url(`/sezione1/validazione/${idSection}`), {
      idSection: idSection,
    });
  }

  getItemMatrice(
    idSezione21: number,
    idSezione1: number,
    idPiao: number
  ): Observable<ItemMatriceDTO[]> {
    let params = new HttpParams().set('idPiao', idPiao).set('idSezione1', idSezione1);
    if (idSezione21) {
      params = params.set('idSezione21', idSezione21);
    }
    return this.http
      .get<GenericResponse<ItemMatriceDTO[]>>(Path.url(`/ovp/matrice`), { params })
      .pipe(map((res) => res.data));
  }

  getSezione1ByIdPiao(idPiao: number): Observable<Sezione1DTO> {
    return this.http
      .get<GenericResponse<Sezione1DTO>>(Path.url(`/sezione1/${idPiao}`))
      .pipe(map((res) => res.data));
  }
}
