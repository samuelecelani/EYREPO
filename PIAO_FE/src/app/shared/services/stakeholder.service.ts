import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, switchMap, tap, Observable } from 'rxjs';
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
        switchMap((savedStakeholder) =>
          this.reloadAndUpdateSession().pipe(
            map(() => savedStakeholder)
          )
        )
      );
  }

  /**
   * Ottiene la lista degli Stakeholder per PIAO
   * GET /stakeholder/piao/{idPiao}
   */
  getByPiao(idPiao: number): Observable<StakeHolderDTO[]> {
    return this.http
      .get<GenericResponse<StakeHolderDTO[]>>(Path.url(`/stakeholder/piao/${idPiao}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Ricarica la lista degli stakeholder dal backend e aggiorna il PIAODTO in session
   */
  private reloadAndUpdateSession(): Observable<StakeHolderDTO[]> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (piaoDTO && piaoDTO.id) {
      return this.getByPiao(piaoDTO.id).pipe(
        tap((stakeholders) => {
          // Aggiorna sia stakeHolders a livello PIAO che nella sezione1
          piaoDTO.stakeHolders = stakeholders;
          if (piaoDTO.sezione1) {
            piaoDTO.sezione1.stakeHolders = stakeholders;
          }
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
