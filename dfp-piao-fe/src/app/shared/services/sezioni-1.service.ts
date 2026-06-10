import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Sezione1DTO } from '../models/classes/sezione-1-dto';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map, Observable } from 'rxjs';
import { ItemMatriceDTO } from '../models/classes/item-matrice-dto';
import { CodPathEnum } from '../models/enums/cod-path.enum';
import { AnagraficaDTO } from '../models/classes/anagrafica-dto';

@Injectable({
  providedIn: 'root',
})
export class Sezione1Service {
  constructor(private http: HttpClient) {}

  save(sezione: Sezione1DTO) {
    return this.http.post<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_1}/save`),
      sezione
    );
  }

  validation(idSection: number, testoSezione: string, campiModificati: string) {
    const codicePa = this.getCodicePaFromSession();
    let params = new HttpParams()
      .set('testoSezione', testoSezione)
      .set('campiModificati', campiModificati);
    if (codicePa) {
      params = params.set('codicePa', codicePa);
    }
    return this.http.patch<GenericResponse<void>>(
      Path.url(`/${CodPathEnum.SEZIONE_1}/validazione/${idSection}`),
      {
        idSection: idSection,
      },
      { params }
    );
  }

  getItemMatrice(
    idSezione21: number,
    idSezione1: number,
    idPiao: number
  ): Observable<ItemMatriceDTO[]> {
    let params = new HttpParams().set('idPiao', idPiao).set('idSezione1', idSezione1);
    if (idSezione21) {
      params = params.set('idSezione21', idSezione21);
    }
    return this.http
      .get<GenericResponse<ItemMatriceDTO[]>>(Path.url(`/ovp/matrice`), { params })
      .pipe(map((res) => (res ? res.data : [])));
  }

  getSezione1ByIdPiao(idPiao: number, codiceFiscale?: string): Observable<Sezione1DTO | null> {
    let params = new HttpParams();
    if (codiceFiscale) {
      params = params.set('codiceFiscale', codiceFiscale);
    }
    return this.http
      .get<
        GenericResponse<Sezione1DTO | null>
      >(Path.url(`/${CodPathEnum.SEZIONE_1}/${idPiao}`), { params })
      .pipe(map((res) => (res ? res.data : null)));
  }

  getAnagrafica(codiceFiscale: string): Observable<AnagraficaDTO | undefined> {
    const params = new HttpParams().set('codiceFiscale', codiceFiscale);
    return this.http
      .get<
        GenericResponse<AnagraficaDTO>
      >(Path.url(`/${CodPathEnum.SEZIONE_1}/anagrafica-ipa`), { params })
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  private getCodicePaFromSession(): string | null {
    try {
      const paAttivaRaw = sessionStorage.getItem('paAttivaDTO');
      if (!paAttivaRaw) return null;
      const paAttiva = JSON.parse(paAttivaRaw);
      return typeof paAttiva?.codePA === 'string' && paAttiva.codePA ? paAttiva.codePA : null;
    } catch {
      return null;
    }
  }
}
