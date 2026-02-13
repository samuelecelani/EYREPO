import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione23Service } from './sezione23.service';
import { switchMap } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class ObiettivoPrevenzioneService {
  sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  delete(id: number) {
    return this.http
      .delete<void>(Path.url(`/obiettivo-prevenzione/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
