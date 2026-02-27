import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map } from 'rxjs';
import { DichiarazioneScadenzaDTO } from '../models/classes/dichiarazione-scadenza-dto';

@Injectable({
  providedIn: 'root',
})
export class DichiarazioneScadenzaService {
  constructor(private http: HttpClient) {}

  getExistingDichiarazioneScadenza(codPAFK: string) {
    return this.http
      .get<
        GenericResponse<DichiarazioneScadenzaDTO>
      >(Path.url(`/dichiarazione-scadenza/${codPAFK}`))
      .pipe(map((res) => res.data));
  }

  save(dichiarazione: DichiarazioneScadenzaDTO) {
    return this.http
      .post<
        GenericResponse<DichiarazioneScadenzaDTO>
      >(Path.url('/dichiarazione-scadenza'), dichiarazione)
      .pipe(map((res) => res.data));
  }
}
