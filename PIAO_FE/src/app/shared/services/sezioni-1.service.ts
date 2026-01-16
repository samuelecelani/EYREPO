import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Sezione1DTO } from '../models/classes/sezione-1-dto';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Sezione1Service {
  constructor(private http: HttpClient) {}

  save(sezione: Sezione1DTO) {
    return this.http
      .post<GenericResponse<Sezione1DTO>>(Path.url('/sezione1/save'), sezione)
      .pipe(map((res) => res.data));
  }

  validation(idSection: number) {
    return this.http.patch<GenericResponse<void>>(Path.url(`/sezione1/validazione/${idSection}`), {
      idSection: idSection,
    });
  }
}
