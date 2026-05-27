import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, switchMap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class RichiestaApprovazioneService {
  constructor(private http: HttpClient) {}

  getRichiestaApprovazione(idPiao: number) {
    return this.http
      .get<GenericResponse<any>>(Path.url(`/richiesta-approvazione/${idPiao}`))
      .pipe(map((res) => res ? res.data : null));
  }

  saveRichiestaApprovazione(data: any) {
    return this.http
      .post<GenericResponse<void>>(Path.url(`/richiesta-approvazione/save`), data)
      .pipe(switchMap(() => this.getRichiestaApprovazione(data.idPiao)));
  }
}
