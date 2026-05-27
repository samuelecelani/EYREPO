import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { switchMap } from 'rxjs';
import { ObiettiviRisultatiFotografiaDTO } from '../models/classes/obiettivi-risultati-fotografia-dto';
import { Path } from '../utils/path';
import { Sezione332Service } from './sezione332.service';

@Injectable({
  providedIn: 'root',
})
export class ObiettiviRisultatiFotografiaService {
  private sezione332Service = inject(Sezione332Service);

  constructor(private http: HttpClient) {}

  save(fotografia: ObiettiviRisultatiFotografiaDTO) {
    return this.http
      .post(Path.url(`/obiettivi-risultati-fotografia/save`), fotografia)
      .pipe(switchMap(() => this.sezione332Service.reloadSezione332AndUpdateSession()));
  }

  delete(id: number, idPiao: number, testoSezione: string, codTipologiaFK: string) {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', codTipologiaFK)
      .set('testoSezione', testoSezione);
    return this.http
      .delete<void>(Path.url(`/obiettivi-risultati-fotografia/${id}`), { params })
      .pipe(switchMap(() => this.sezione332Service.reloadSezione332AndUpdateSession()));
  }
}
