import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';

@Injectable({
  providedIn: 'root'
})
export class MinervaService {

  constructor(private http: HttpClient) { }


  getTabellaMock(): Observable<GenericResponse<Map<string, any>>> {
    return this.http.get<GenericResponse<Map<string, any>>>(
      Path.url('/tabella')
    );
  }
}
