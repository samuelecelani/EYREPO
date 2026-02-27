import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione23Service } from './sezione23.service';
import { map, switchMap } from 'rxjs';
import { MisuraPrevenzioneEventoRischioService } from './sezione23/misura-prevenzione-evento-rischio.service';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class MisuraPrevenzioneService {
  sezione23Service: Sezione23Service = inject(Sezione23Service);
  constructor(private http: HttpClient) {}
}
