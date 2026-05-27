import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { map, Observable } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';

@Injectable({
  providedIn: 'root',
})
export class AmministrazioneService {
  constructor(private http: HttpClient) {}

  getTipologieAmministrazioni(): Observable<string[]> {
    return this.http
      .get<GenericResponse<string[]>>(Path.url('/amministrazione/tipologie'))
      .pipe(map((res) => res.data || []));
  }
}
