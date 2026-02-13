import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AttivitaSensibileDTO } from '../models/classes/attivita-sensibile-dto';
import { Path } from '../utils/path';
import { switchMap } from 'rxjs';
import { Sezione23Service } from './sezione23.service';

@Injectable({
  providedIn: 'root',
})
export class AttivitaSensibileRischioService {
  private sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  save(attivita: AttivitaSensibileDTO) {
    return this.http
      .post(Path.url(`/attivita-sensibile/save`), attivita)
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  delete(id: number) {
    return this.http
      .delete<void>(Path.url(`/attivita-sensibile/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
