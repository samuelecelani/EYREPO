import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, switchMap, tap, Observable, shareReplay } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { StakeHolderDTO } from '../models/classes/stakeholder-dto';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';

@Injectable({
  providedIn: 'root',
})
export class StakeholderService {
  private sessionStorageService = inject(SessionStorageService);
  private cache = new Map<number, Observable<StakeHolderDTO[]>>();

  constructor(private http: HttpClient) {}

  /**
   * Salva un nuovo Stakeholder o aggiorna uno esistente
   * POST /stakeholder/save
   * Dopo il salvataggio, ricarica la lista e aggiorna il PIAODTO in session
   */
  save(stakeholder: StakeHolderDTO): Observable<StakeHolderDTO> {
    return this.http
      .post<GenericResponse<StakeHolderDTO>>(Path.url('/stakeholder/save'), stakeholder)
      .pipe(
        map((res) => res.data),
        tap(() => this.clearCache()),
        switchMap((savedStakeholder) =>
          this.reloadAndUpdateSession().pipe(map(() => savedStakeholder))
        )
      );
  }

  /**
   * Ottiene la lista degli Stakeholder per PIAO
   * GET /stakeholder/piao/{idPiao}
   */
  getByPiao(idPiao: number): Observable<StakeHolderDTO[]> {
    if (!this.cache.has(idPiao)) {
      this.cache.set(
        idPiao,
        this.http
          .get<GenericResponse<StakeHolderDTO[]>>(Path.url(`/stakeholder/piao/${idPiao}`))
          .pipe(
            map((res) => (res.data || []).sort((a, b) => (a.id ?? 0) - (b.id ?? 0))),
            shareReplay(1)
          )
      );
    }
    return this.cache.get(idPiao)!;
  }

  clearCache(): void {
    this.cache.clear();
  }

  // DELETE
  delete(id: number): Observable<void> {
    return this.http.delete<void>(Path.url(`/stakeholder/${id}`)).pipe(
      tap(() => this.clearCache()),
      switchMap(() => this.reloadAndUpdateSession()),
      map(() => void 0)
    );
  }

  /**
   * Ricarica la lista degli stakeholder dal backend e aggiorna il PIAODTO in session
   */
  private reloadAndUpdateSession(): Observable<StakeHolderDTO[]> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (piaoDTO && piaoDTO.id) {
      return this.getByPiao(piaoDTO.id).pipe(
        tap((stakeholders) => {
          piaoDTO.stakeHolders = stakeholders;
          this.sessionStorageService.setItem(KEY_PIAO, piaoDTO);
        })
      );
    }

    return new Observable((observer) => {
      observer.next([]);
      observer.complete();
    });
  }
}
