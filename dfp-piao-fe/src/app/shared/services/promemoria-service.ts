import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, switchMap } from 'rxjs';
import { Sezione4Service } from './sezione4.service';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { PromemoriaDTO } from '../models/classes/promemoria-dto';

@Injectable({
  providedIn: 'root',
})
export class PromemoriaService {
  constructor(private http: HttpClient) {}

  getAll() {
    return this.http.get<GenericResponse<PromemoriaDTO[]>>(Path.url(`/promemoria`));
  }
}
