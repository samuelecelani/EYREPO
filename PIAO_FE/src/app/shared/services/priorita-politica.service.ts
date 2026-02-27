import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, switchMap, tap, Observable } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { PrioritaPoliticaDTO } from '../models/classes/priorita-politica-dto';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';

@Injectable({
  providedIn: 'root',
})
export class PrioritaPoliticaService {
  private sessionStorageService = inject(SessionStorageService);

  constructor(private http: HttpClient) {}

  /**
   * Salva una nuova Priorità politica o aggiorna una esistente
   * POST /priorita-politiche/save
   * Dopo il salvataggio, ricarica la lista e aggiorna il PIAODTO in session
   */
  save(prioritaPolitica: PrioritaPoliticaDTO): Observable<PrioritaPoliticaDTO> {
    return this.http
      .post<
        GenericResponse<PrioritaPoliticaDTO>
      >(Path.url('/priorita-politiche/save'), prioritaPolitica)
      .pipe(
        map((res) => res.data),
        switchMap((savedPrioritaPolitica) =>
          this.reloadAndUpdateSession().pipe(map(() => savedPrioritaPolitica))
        )
      );
  }

  /**
   * Ottiene la lista delle Priorità politiche per Sezione 1
   * GET /priorita-politiche/sezione/{idSezione1}
   */
  getBySezione(idSezione1: number): Observable<PrioritaPoliticaDTO[]> {
    return this.http
      .get<
        GenericResponse<PrioritaPoliticaDTO[]>
      >(Path.url(`/priorita-politiche/sezione1/${idSezione1}`))
      .pipe(map((res) => res.data));
  }
  /**
   * Ottiene la lista delle Priorità politiche per Sezione 1
   * GET /priorita-politiche/sezione/{idSezione1}
   */
  getByIdPiao(idPiao: number): Observable<PrioritaPoliticaDTO[]> {
    return this.http
      .get<GenericResponse<PrioritaPoliticaDTO[]>>(Path.url(`/priorita-politiche/piao/${idPiao}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Ricarica la lista delle priorità politiche dal backend e aggiorna il PIAODTO in session
   */
  private reloadAndUpdateSession(): Observable<PrioritaPoliticaDTO[]> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (piaoDTO && piaoDTO.idSezione1) {
      return this.getBySezione(piaoDTO.idSezione1).pipe(
        tap((prioritaPolitiche) => {
          console.log(
            '[PrioritaPoliticaService] Priorità politiche reloaded, updating session',
            prioritaPolitiche
          );
        })
      );
    } else if (piaoDTO && piaoDTO.id) {
      return this.getByIdPiao(piaoDTO.id).pipe(
        tap((prioritaPolitiche) => {
          console.log(
            '[PrioritaPoliticaService] Priorità politiche reloaded by idPiao, updating session',
            prioritaPolitiche
          );
        })
      );
    }

    return new Observable((observer) => {
      observer.next([]);
      observer.complete();
    });
  }
}
