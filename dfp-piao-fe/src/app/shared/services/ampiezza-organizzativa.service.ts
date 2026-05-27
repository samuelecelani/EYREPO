import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione31Service } from './sezione31.service';
import { switchMap } from 'rxjs';
import { AmpiezzaOrganizzativaDTO } from '../models/classes/ampiezza-organizzativa-dto';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class AmpiezzaOrganizzativaService {
  private sezione31Service = inject(Sezione31Service);

  constructor(private http: HttpClient) {}

  save(ampiezza: AmpiezzaOrganizzativaDTO) {
    return this.http
      .post(Path.url(`/ampiezza-organizzativa/save`), ampiezza)
      .pipe(switchMap(() => this.sezione31Service.reloadSezione31AndUpdateSession()));
  }

  delete(id: number, idPiao: number, testoSezione: string) {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'ampiezzaOrganizzativa')
      .set('testoSezione', testoSezione);

    return this.http
      .delete<void>(Path.url(`/ampiezza-organizzativa/${id}`), { params })
      .pipe(switchMap(() => this.sezione31Service.reloadSezione31AndUpdateSession()));
  }
}
