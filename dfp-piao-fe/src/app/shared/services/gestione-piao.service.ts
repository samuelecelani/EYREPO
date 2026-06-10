import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AmministrazioneInternalDTO } from '../models/classes/amministrazione-internal-dto';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class GestionePiaoService {
  constructor(private http: HttpClient) {}

  getAmministrazioneByTipologiaAndDenomAndCode(
    tipologia?: string,
    denominazione?: string,
    codicePA?: string
  ) {
    let params = new HttpParams();
    if (tipologia) {
      params = params.set('tipologia', tipologia);
    }
    if (denominazione) {
      params = params.set('denominazione', denominazione);
    }
    if (codicePA) {
      params = params.set('codiceIpa', codicePA);
    }
    return this.http
      .get<
        GenericResponse<AmministrazioneInternalDTO[]>
      >(Path.url('/amministrazione/search'), { params })
      .pipe(map((response) => response.data || []));
  }
}
