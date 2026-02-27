import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { GenericResponse } from '../models/interfaces/generic-response';
import { DImensioneIndicatoreDTO } from '../models/classes/dimensione-indicatore-dto';
import { map, Observable, shareReplay } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class DimensioneIndicatoreService {
  private cache = new Map<string, Observable<DImensioneIndicatoreDTO[]>>();

  constructor(private http: HttpClient) {}

  getDimensioniIndicatore(codTipologiaFK: string) {
    if (!this.cache.has(codTipologiaFK)) {
      const params = new HttpParams().set('codTipologiaFK', codTipologiaFK);

      const request$ = this.http
        .get<GenericResponse<DImensioneIndicatoreDTO[]>>(Path.url(`/dimensione-indicatore`), {
          params,
        })
        .pipe(
          map((res) => res.data),
          shareReplay(1)
        );

      this.cache.set(codTipologiaFK, request$);
    }

    return this.cache.get(codTipologiaFK)!;
  }

  clearCache(): void {
    this.cache.clear();
  }
}
