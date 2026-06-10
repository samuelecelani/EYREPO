import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable, shareReplay } from 'rxjs';
import { TargetIndicatoreDTO } from '../models/classes/target-indicatore-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class TargetIndicatoreService {
  private cache$?: Observable<TargetIndicatoreDTO[]>;

  constructor(private http: HttpClient) {}

  getTargetIndicatore() {
    if (!this.cache$) {
      this.cache$ = this.http
        .get<GenericResponse<TargetIndicatoreDTO[]>>(Path.url(`/target-indicatore`))
        .pipe(
          map((res) => res.data),
          shareReplay(1)
        );
    }

    return this.cache$;
  }

  clearCache(): void {
    this.cache$ = undefined;
  }
}
