import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { GenericResponse } from '../models/interfaces/generic-response';
import { DImensioneIndicatoreDTO } from '../models/classes/dimensione-indicatore-dto';
import { map } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class DimensioneIndicatoreService {
  constructor(private http: HttpClient) {}

  getDimensioniIndicatore(codTipologiaFK: string) {
    const params = new HttpParams().set('codTipologiaFK', codTipologiaFK);

    return this.http
      .get<GenericResponse<DImensioneIndicatoreDTO[]>>(Path.url(`/dimensione-indicatore`), {
        params,
      })
      .pipe(map((res) => res.data));
  }
}
