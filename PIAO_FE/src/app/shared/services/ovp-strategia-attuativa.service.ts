import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { switchMap, map } from 'rxjs';
import { OVPStrategiaDTO } from '../models/classes/ovp-strategia-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione21Service } from './sezioni-21.service';

@Injectable({
  providedIn: 'root',
})
export class OvpStrategiaAttuativaService {
  private sezione21Service = inject(Sezione21Service);

  constructor(private http: HttpClient) {}

  save(idObiettivoOVP: number, ovpStrategiaDTO: OVPStrategiaDTO) {
    return this.http
      .post<
        GenericResponse<void>
      >(Path.url(`/ovp-strategia/save/${idObiettivoOVP}`), ovpStrategiaDTO)
      .pipe(switchMap(() => this.sezione21Service.reloadSezione21AndUpdateSession()));
  }

  delete(idStrategia: number) {
    return this.http
      .delete<GenericResponse<void>>(Path.url(`/ovp-strategia/delete/${idStrategia}`))
      .pipe(switchMap(() => this.sezione21Service.reloadSezione21AndUpdateSession()));
  }

  getAllStrategiaByIdOVP(idOVP: number) {
    return this.http
      .get<GenericResponse<OVPStrategiaDTO[]>>(Path.url(`/ovp-strategia/ovp/${idOVP}`))
      .pipe(map((res) => res.data));
  }
}
