import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { TipologiaTabellaSezione331 } from '../models/enums/tipologia-tabella-sezione3-3-1.enum';
import { CodPathEnum } from '../models/enums/cod-path.enum';

@Injectable({
  providedIn: 'root',
})
export class MinervaService {
  constructor(private http: HttpClient) {}

  getTabellaMock(tipoTabella: string): Observable<Map<string, any>> {
    return this.http
      .get<
        GenericResponse<Map<string, any>>
      >(Path.url(`/${CodPathEnum.SEZIONE_3_3_1}/tabella/${tipoTabella}`))
      .pipe(map((res) => res.data));
  }
  getTabella(
    codiceAmministrazione: string,
    annoRiferimento: string,
    idEntitaFK: number,
    storageMinerva: boolean
  ): Observable<Map<string, any>[] | null> {
    const params = new HttpParams()
      .set('codiceAmministrazione', codiceAmministrazione)
      .set('annoRiferimento', annoRiferimento)
      .set('idEntitaFk', idEntitaFK)
      .set('storageMinerva', storageMinerva);
    return this.http
      .get<
        GenericResponse<Map<string, any>[]>
      >(Path.url(`/${CodPathEnum.SEZIONE_3_3_1}/tabelle`), { params })
      .pipe(map((res) => (res ? res.data : null)));
  }
}
