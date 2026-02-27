import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { IndicatoreDTO } from '../models/classes/indicatore-dto';
import { map, Observable, of, switchMap } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione21Service } from './sezioni-21.service';
import { Sezione22Service } from './sezioni-22.service';
import { SectionEnum } from '../models/enums/section.enum';
import { Sezione23Service } from './sezione23.service';

@Injectable({
  providedIn: 'root',
})
export class IndicatoreService {
  constructor(private http: HttpClient) {}
  private sezione21Service = inject(Sezione21Service);
  private sezione22Service = inject(Sezione22Service);

  saveIndicatore(indicatore: IndicatoreDTO, isNewIndicatore: boolean, sectionEnum: SectionEnum) {
    return this.http
      .post<GenericResponse<IndicatoreDTO>>(Path.url('/indicatore/save'), indicatore)
      .pipe(map((res) => res.data));
  }

  getIndicatoriByIdPiaoAndIdEntitaAndCodTipologia(
    idPiao: number,
    idEntitaFK: number,
    codTipologiaIndicatore: string
  ): Observable<IndicatoreDTO[]> {
    const params = new HttpParams()
      .set('idPiao', idPiao)
      .set('idEntitaFK', idEntitaFK)
      .set('codTipologiaFK', codTipologiaIndicatore);

    return this.http
      .get<GenericResponse<IndicatoreDTO[]>>(Path.url('/indicatore'), {
        params,
      })
      .pipe(map((res) => res.data));
  }
}
