import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione23Service } from './sezione23.service';
import { map, switchMap } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { ObiettivoPrevenzioneDTO } from '../models/classes/obiettivo-prevenzione-dto';

@Injectable({
  providedIn: 'root',
})
export class ObiettivoPrevenzioneService {
  sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  delete(id: number) {
    return this.http
      .delete<void>(Path.url(`/obiettivo-prevenzione/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  save(obiettivoPrevenzione: any) {
    return this.http
      .post<
        GenericResponse<ObiettivoPrevenzioneDTO>
      >(Path.url(`/obiettivo-prevenzione/save`), obiettivoPrevenzione)
      .pipe(map((res) => res.data));
  }
}
