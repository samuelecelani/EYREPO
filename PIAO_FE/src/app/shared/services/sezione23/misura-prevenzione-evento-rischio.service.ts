import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione23Service } from '../sezione23.service';
import { Path } from '../../utils/path';
import { map, switchMap } from 'rxjs';
import { GenericResponse } from '../../models/interfaces/generic-response';

@Injectable({
  providedIn: 'root',
})
export class MisuraPrevenzioneEventoRischioService {
  private sezione23Service: Sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  deleteAllByIdEventoRischioso(idEventoRischioso: number) {
    return this.http
      .delete(Path.url(`/misura-prevenzione-evento-rischio/eventoRischio/${idEventoRischioso}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  delete(id: number) {
    return this.http
      .delete(Path.url(`/misura-prevenzione-evento-rischio/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  save(misuraPre: any) {
    return this.http
      .post<GenericResponse<any>>(Path.url(`/misura-prevenzione-evento-rischio/save`), misuraPre)
      .pipe(map((res) => res.data));
  }
}
