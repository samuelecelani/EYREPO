import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MotivazioneDichiarazioneDTO } from '../models/classes/motivazione-dichiarazione-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class MotivazioneDichiarazioneService {
  constructor(private http: HttpClient) {}

  getAllMotivazioniDichiarazione() {
    return this.http
      .get<GenericResponse<MotivazioneDichiarazioneDTO[]>>(Path.url(`/motivazione-dichiarazione`))
      .pipe(map((res) => res.data || []));
  }
}
