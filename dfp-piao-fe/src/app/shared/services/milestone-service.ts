import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of, switchMap, tap } from 'rxjs';
import { Sezione4Service } from './sezione4.service';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { MilestoneDTO } from '../models/classes/milestone-dto';
import { PromemoriaDTO } from '../models/classes/promemoria-dto';

@Injectable({
  providedIn: 'root',
})
export class MilestoneService {
  private sezione4Service: Sezione4Service = inject(Sezione4Service);

  constructor(private http: HttpClient) {}

  /**
   * Salva o aggiorna una milestone con idPromemoria e dataPromemoria
   * POST /api/v1/milestone/save
   * Restituisce successo anche se il reload della sezione4 fallisce
   */
  saveOrUpdate(milestone: MilestoneDTO): Observable<GenericResponse<MilestoneDTO>> {
    return this.http
      .post<GenericResponse<MilestoneDTO>>(Path.url('/milestone/save'), milestone)
      .pipe(
        tap(() => {
          // Tenta il reload in background, ma non blocca il flusso se fallisce
          this.sezione4Service.reloadSezione4AndUpdateSession().subscribe({
            error: (err) => {
              console.warn(
                'Errore nel reload della sezione4 dopo il salvataggio della milestone:',
                err
              );
              // Non mostriamo errore all'utente perché la POST è andata a buon fine
            },
          });
        })
      );
  }

  /**
   * Ottiene tutte le milestone per una sottofase monitoraggio
   * GET /api/v1/milestone/sottofase-monitoraggio/{idSottofaseMonitoraggio}
   */
  getBySottofaseMonitoraggio(idSottofaseMonitoraggio: number): Observable<MilestoneDTO[]> {
    return this.http
      .get<
        GenericResponse<MilestoneDTO[]>
      >(Path.url(`/milestone/sottofase-monitoraggio/${idSottofaseMonitoraggio}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Ottiene il promemoria per una milestone
   * GET /api/v1/milestone/promemoria/{idMilestone}
   */
  getPromemoriaByMilestone(idMilestone: number): Observable<PromemoriaDTO> {
    return this.http
      .get<GenericResponse<PromemoriaDTO>>(Path.url(`/milestone/promemoria/${idMilestone}`))
      .pipe(map((res) => res.data));
  }

  /**
   * Elimina una milestone
   * DELETE /api/v1/milestone/{id}
   * Restituisce successo anche se il reload della sezione4 fallisce
   */
  delete(id: number, idPiao: number, testoSezione: string): Observable<GenericResponse<void>> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'milestone')
      .set('testoSezione', testoSezione);
    return this.http.delete<GenericResponse<void>>(Path.url(`/milestone/${id}`), { params }).pipe(
      tap(() => {
        // Tenta il reload in background, ma non blocca il flusso se fallisce
        this.sezione4Service.reloadSezione4AndUpdateSession().subscribe({
          error: (err) => {
            console.warn(
              "Errore nel reload della sezione4 dopo l'eliminazione della milestone:",
              err
            );
            // Non mostriamo errore all'utente perché la DELETE è andata a buon fine
          },
        });
      })
    );
  }
}
