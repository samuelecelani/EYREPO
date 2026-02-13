import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, switchMap, tap, Observable } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { AreaOrganizzativaDTO } from '../models/classes/area-organizzativa-dto';
import { SessionStorageService } from './session-storage.service';
import { KEY_PIAO } from '../utils/constants';
import { PIAODTO } from '../models/classes/piao-dto';

@Injectable({
  providedIn: 'root',
})
export class AreaOrganizzativaService {
  private sessionStorageService = inject(SessionStorageService);

  constructor(private http: HttpClient) {}

  /**
   * Salva una nuova Area organizzativa o aggiorna una esistente
   * POST /aree-organizzative/save
   * Dopo il salvataggio, ricarica la lista e aggiorna il PIAODTO in session
   */
  save(areaOrganizzativa: AreaOrganizzativaDTO): Observable<AreaOrganizzativaDTO> {
    return this.http
      .post<GenericResponse<AreaOrganizzativaDTO>>(Path.url('/aree-organizzative/save'), areaOrganizzativa)
      .pipe(
        map((res) => res.data),
        switchMap((savedAreaOrganizzativa) =>
          this.reloadAndUpdateSession().pipe(
            map(() => savedAreaOrganizzativa)
          )
        )
      );
  }

  /**
   * Ottiene la lista delle Aree organizzative per Sezione 1
   * GET /aree-organizzative/sezione/{idSezione1}
   */
  getBySezione(idSezione1: number): Observable<AreaOrganizzativaDTO[]> {
    return this.http
      .get<GenericResponse<AreaOrganizzativaDTO[]>>(Path.url(`/aree-organizzative/sezione/${idSezione1}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Ricarica la lista delle aree organizzative dal backend e aggiorna il PIAODTO in session
   */
  private reloadAndUpdateSession(): Observable<AreaOrganizzativaDTO[]> {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (piaoDTO && piaoDTO.sezione1?.id) {
      return this.getBySezione(piaoDTO.sezione1.id).pipe(
        tap((areeOrganizzative) => {
          if (piaoDTO.sezione1) {
            piaoDTO.sezione1.areeOrganizzative = areeOrganizzative;
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
