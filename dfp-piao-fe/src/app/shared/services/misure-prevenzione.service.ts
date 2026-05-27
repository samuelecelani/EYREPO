import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione23Service } from './sezione23.service';
import { map, switchMap } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';

@Injectable({
  providedIn: 'root',
})
export class MisurePrevenzioneService {
  sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  delete(id: number) {
    return this.http
      .delete<void>(Path.url(`/misura-prevenzione/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  save(misura: any) {
    return this.http
      .post<GenericResponse<void>>(Path.url(`/misura-prevenzione/save`), misura)
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
