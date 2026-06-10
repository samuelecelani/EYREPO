import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { StatoStoricoSezioneDTO } from '../models/classes/stato-storico-sezione-dto';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class StoricoStatoSezioneService {
  constructor(private httpClient: HttpClient) {}

  save(statoStoricoSezioneDTO: StatoStoricoSezioneDTO) {
    return this.httpClient
      .post<
        GenericResponse<StatoStoricoSezioneDTO>
      >(Path.url('/storico-stato-sezione/save'), statoStoricoSezioneDTO)
      .pipe(map((response) => response.data));
  }
}
