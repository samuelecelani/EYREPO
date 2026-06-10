import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { map, switchMap } from 'rxjs';
import { Sezione23Service } from './sezione23.service';
import { GenericResponse } from '../models/interfaces/generic-response';
import { ObiettivoPrevenzioneCorruzioneTrasparenzaDTO } from '../models/classes/obiettivo-prevenzione-corruzione-trasparenza-dto';

@Injectable({
  providedIn: 'root',
})
export class ObiettivoPrevenzioneCorruzioneTrasparenzaService {
  constructor(private http: HttpClient) {}

  private sezione23Service = inject(Sezione23Service);

  delete(id: number, idPiao: number, testoSezione: string) {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('testoSezione', testoSezione)
      .set('campiModificati', 'valorePubblico');
    return this.http
      .delete<void>(Path.url(`/obiettivo-prevenzione-corruzione-trasparenza/${id}`), { params })
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  save(obb: any) {
    return this.http
      .post<
        GenericResponse<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>
      >(Path.url(`/obiettivo-prevenzione-corruzione-trasparenza/save`), obb)
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
