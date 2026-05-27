import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione31Service } from './sezione31.service';
import { Observable, of, switchMap } from 'rxjs';
import { TabellaFunzionaleDTO } from '../models/classes/tabella-funzionale-dto';
import { Path } from '../utils/path';
import { Sezione32Service } from './sezione32.service';
import { Sezione331Service } from './sezione331.service';
import { Sezione332Service } from './sezione332.service';
import { SectionEnum } from '../models/enums/section.enum';

@Injectable({
  providedIn: 'root',
})
export class TabellaFunzionaleService {
  private sezione31Service = inject(Sezione31Service);
  private sezione32Service = inject(Sezione32Service);
  private sezione331Service = inject(Sezione331Service);
  private sezione332Service = inject(Sezione332Service);

  constructor(private http: HttpClient) {}

  private reloadByCodTipologia(codTipologiaFK?: string): Observable<any> {
    switch (codTipologiaFK) {
      case SectionEnum.SEZIONE_3_1:
        return this.sezione31Service.reloadSezione31AndUpdateSession();
      case SectionEnum.SEZIONE_3_2:
        return this.sezione32Service.reloadSezione32AndUpdateSession();
      case SectionEnum.SEZIONE_3_3_1:
        return this.sezione331Service.reloadSezione331AndUpdateSession();
      case SectionEnum.SEZIONE_3_3_2:
        return this.sezione332Service.reloadSezione332AndUpdateSession();
      default:
        return of(undefined);
    }
  }

  save(tabella: TabellaFunzionaleDTO) {
    return this.http
      .post(Path.url(`/tabella-funzionale/save`), tabella)
      .pipe(switchMap(() => this.reloadByCodTipologia(tabella.codTipologiaFK)));
  }

  delete(id: number, idPiao: number, codTipologiaFK: string, testoSezione: string) {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('codTipologiaFK', codTipologiaFK)
      .set('campiModificati', 'tabellaFunzionale')
      .set('testoSezione', testoSezione);
    return this.http
      .delete<void>(Path.url(`/tabella-funzionale/${id}`), { params })
      .pipe(switchMap(() => this.reloadByCodTipologia(codTipologiaFK)));
  }
}
