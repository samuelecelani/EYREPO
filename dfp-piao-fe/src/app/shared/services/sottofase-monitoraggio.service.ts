import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of, switchMap, tap } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { Sezione4Service } from './sezione4.service';
import { SottofaseMonitoraggioDTO } from '../models/classes/sezione-4-dto';
import { Sezione4DTO } from '../models/classes/sezione-4-dto';

@Injectable({
  providedIn: 'root',
})
export class SottofaseMonitoraggioService {
  private sezione4Service = inject(Sezione4Service);

  constructor(private http: HttpClient) {}

  /**
   * Salva una nuova sottofase monitoraggio o aggiorna una esistente
   * POST /api/v1/sottofase-monitoraggio/save
   * Restituisce successo anche se il reload della sezione4 fallisce
   */
  save(sottofase: SottofaseMonitoraggioDTO): Observable<GenericResponse<SottofaseMonitoraggioDTO>> {
    return this.http
      .post<
        GenericResponse<SottofaseMonitoraggioDTO>
      >(Path.url('/sottofase-monitoraggio/save'), sottofase)
      .pipe(
        switchMap((response) =>
          this.sezione4Service.reloadSezione4AndUpdateSession().pipe(
            map(() => response),
            catchError((err) => {
              console.warn(
                'Errore nel reload della sezione4 dopo il salvataggio della sottofase:',
                err
              );
              return of(response);
            })
          )
        )
      );
  }

  /**
   * Ottiene tutte le sottofasi monitoraggio per una sezione4
   * GET /api/v1/sottofase-monitoraggio/sezione4/{idSezione4}
   */
  getBySezione4(idSezione4: number): Observable<SottofaseMonitoraggioDTO[]> {
    return this.http
      .get<
        GenericResponse<SottofaseMonitoraggioDTO[]>
      >(Path.url(`/sottofase-monitoraggio/sezione4/${idSezione4}`))
      .pipe(map((res) => (res ? res.data : [])));
  }

  /**
   * Elimina una sottofase monitoraggio
   * DELETE /api/v1/sottofase-monitoraggio/{id}
   * Restituisce successo anche se il reload della sezione4 fallisce
   */
  delete(id: number, idPiao: number, testoSezione: string): Observable<GenericResponse<void>> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'sottofaseMonitoraggio')
      .set('testoSezione', testoSezione);

    return this.http
      .delete<GenericResponse<void>>(Path.url(`/sottofase-monitoraggio/${id}`), { params })
      .pipe(
        switchMap((response) =>
          this.sezione4Service.reloadSezione4AndUpdateSession().pipe(
            map(() => response),
            catchError((err) => {
              console.warn(
                "Errore nel reload della sezione4 dopo l'eliminazione della sottofase:",
                err
              );
              return of(response);
            })
          )
        )
      );
  }
}
