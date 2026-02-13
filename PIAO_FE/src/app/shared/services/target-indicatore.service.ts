import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { TargetIndicatoreDTO } from '../models/classes/target-indicatore-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class TargetIndicatoreService {
  constructor(private http: HttpClient) {}

  getTargetIndicatore() {
    return this.http
      .get<GenericResponse<TargetIndicatoreDTO[]>>(Path.url(`/target-indicatore`))
      .pipe(map((res) => res.data));
  }
}
