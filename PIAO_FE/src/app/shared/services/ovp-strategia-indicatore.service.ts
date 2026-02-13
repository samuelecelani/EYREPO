import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { IndicatoreDTO } from '../models/classes/indicatore-dto';
import { switchMap } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione21Service } from './sezioni-21.service';
import { OVPStrategiaIndicatoreDTO } from '../models/classes/ovp-strategia-indicatore-dto';

@Injectable({
  providedIn: 'root',
})
export class OvpStrategiaIndicatoreService {
  constructor(private http: HttpClient) {}
  private sezione21Service = inject(Sezione21Service);

  save(idStrategia: number, strategiaIndicatoreDTO: OVPStrategiaIndicatoreDTO) {
    return this.http
      .post<
        GenericResponse<OVPStrategiaIndicatoreDTO>
      >(Path.url(`/ovp-strategia-indicatore/save/${idStrategia}`), strategiaIndicatoreDTO)
      .pipe(switchMap(() => this.sezione21Service.reloadSezione21AndUpdateSession()));
  }

  delete(idOVPStrategiaIndicatore: number) {
    return this.http
      .delete<
        GenericResponse<void>
      >(Path.url(`/ovp-strategia-indicatore/delete/${idOVPStrategiaIndicatore}`))
      .pipe(switchMap(() => this.sezione21Service.reloadSezione21AndUpdateSession()));
  }
}
