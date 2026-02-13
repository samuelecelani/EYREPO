import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { switchMap } from 'rxjs';
import { Sezione23Service } from './sezione23.service';

@Injectable({
  providedIn: 'root',
})
export class ObiettivoPrevenzioneCorruzioneTrasparenzaService {
  constructor(private http: HttpClient) {}

  private sezione23Service = inject(Sezione23Service);

  delete(id: number) {
    return this.http
      .delete<void>(Path.url(`/obiettivo-prevenzione-corruzione-trasparenza/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
