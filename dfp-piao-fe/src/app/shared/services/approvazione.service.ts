import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ApprovazioneDTO } from '../models/classes/approvazione-dto';
import { map, Observable, of, Subject, switchMap, tap } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { PIAODTO } from '../models/classes/piao-dto';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';

@Injectable({
  providedIn: 'root',
})
export class ApprovazioneService {
  constructor(private httpClient: HttpClient) {}

  private sessionStorageService = inject(SessionStorageService);

  // Subject per notificare quando l'approvazione viene aggiornata
  private approvazioneUpdated$ = new Subject<ApprovazioneDTO>();

  // Observable pubblico per sottoscriversi agli aggiornamenti
  public onApprovazioneUpdated$ = this.approvazioneUpdated$.asObservable();

  saveApprovazione(approvazione: ApprovazioneDTO) {
    return this.httpClient
      .put<GenericResponse<void>>(Path.url(`/piao/approvazione`), approvazione)
      .pipe(switchMap(() => this.reloadApprovazioneAndUpdateSession()));
  }

  getApprovazione(idPiao: number) {
    return this.httpClient
      .get<GenericResponse<ApprovazioneDTO>>(Path.url(`/piao/approvazione/${idPiao}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Metodo centralizzato per ricaricare l'approvazione e notificare i subscriber
   */
  reloadApprovazioneAndUpdateSession(): Observable<ApprovazioneDTO | undefined> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (piaoDTO?.id) {
      return this.getApprovazione(piaoDTO.id).pipe(
        tap((approvazione) => {
          this.approvazioneUpdated$.next(approvazione);
        })
      );
    }

    return of(undefined);
  }
}
