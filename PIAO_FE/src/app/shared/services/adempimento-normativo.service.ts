import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione23Service } from './sezione23.service';
import { switchMap } from 'rxjs';
import { AdempimentoNormativoDTO } from '../models/classes/adempimento-normativo-dto';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class AdempimentoNormativoService {
  private sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  save(adempimento: AdempimentoNormativoDTO) {
    return this.http
      .post(Path.url(`/adempimenti-normativi`), adempimento)
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  delete(id: number) {
    return this.http
      .delete<void>(Path.url(`/adempimenti-normativi/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
