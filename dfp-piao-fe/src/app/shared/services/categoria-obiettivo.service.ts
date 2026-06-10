import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { CategoriaObiettiviDTO } from '../models/classes/categoria-obiettivi-dto';
import { Sezione4Service } from './sezione4.service';
import { map, switchMap } from 'rxjs';
import { Path } from '../utils/path';
import { CategoriaObiettiviTipDTO } from '../models/classes/categoria-obiettivi-tip-dto';
import { GenericResponse } from '../models/interfaces/generic-response';

@Injectable({
  providedIn: 'root',
})
export class CategoriaObiettivoService {
  constructor(private http: HttpClient) {}

  private sezione4: Sezione4Service = inject(Sezione4Service);

  save(categoriaObiettiviDTO: CategoriaObiettiviDTO) {
    return this.http
      .post(Path.url(`/categoria-obiettivi/save`), categoriaObiettiviDTO)
      .pipe(switchMap(() => this.sezione4.reloadSezione4AndUpdateSession()));
  }

  delete(id: number, idPiao: number, testoSezione: string, formArrayName: string) {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', formArrayName)
      .set('testoSezione', testoSezione);
    return this.http
      .delete<void>(Path.url(`/categoria-obiettivi/${id}`), { params })
      .pipe(switchMap(() => this.sezione4.reloadSezione4AndUpdateSession()));
  }

  getAllCategoriaObiettiviTip(codTipologiaFK: string) {
    const params = new HttpParams().set('codTipologiaFK', codTipologiaFK);
    return this.http
      .get<
        GenericResponse<CategoriaObiettiviTipDTO[]>
      >(Path.url(`/categoria-obiettivi/getAllCategoriaObiettivi`), { params })
      .pipe(map((res) => (res ? res.data || [] : [])));
  }
}
