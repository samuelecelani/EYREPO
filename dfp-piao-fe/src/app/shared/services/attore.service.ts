import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { AttoreDTO } from '../models/classes/attore-dto';
import { map, Observable } from 'rxjs';
@Injectable({
  providedIn: 'root',
})
export class AttoreService {
  constructor(private http: HttpClient) {}

  getAll(idPiao: number): Observable<AttoreDTO[]> {
    return this.http
      .get<GenericResponse<AttoreDTO[]>>(Path.url(`/attore/piao/${idPiao}`))
      .pipe(map((res) => res.data ?? []));
  }

  getAttoreByTipologiaAndIdPiao(
    codTipologiaAttore: string,
    idPiao: number
  ): Observable<AttoreDTO[]> {
    return this.http
      .get<
        GenericResponse<AttoreDTO[]>
      >(Path.url(`/attore/piao/${idPiao}/sezione/${codTipologiaAttore}`))
      .pipe(map((res) => res.data ?? []));
  }

  getAttoreByTipologiaAndExternalId(
    codTipologiaAttore: string,
    externalId: number | string
  ): Observable<AttoreDTO> {
    return this.http
      .get<
        GenericResponse<AttoreDTO>
      >(Path.url(`/attore/external/${externalId}/sezione/${codTipologiaAttore}`))
      .pipe(map((res) => (res && res.data) ?? null));
  }
}
