import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { AttivitaFormativeDTO } from '../models/classes/attivita-formativa-dto';

@Injectable({
  providedIn: 'root',
})
export class AttivitaFormativeService {
  private http = inject(HttpClient);

  /**
   * GET /api/v1/attivita-formative/sezione332/{idSezione332}
   * Recupera la lista delle attività formative per una sezione 332
   */
  getBySezione332(idSezione332: number): Observable<AttivitaFormativeDTO[]> {
    return this.http
      .get<
        GenericResponse<AttivitaFormativeDTO[]>
      >(Path.url(`/attivita-formative/sezione332/${idSezione332}`))
      .pipe(map((res) => (res ? res.data || [] : [])));
  }

  /**
   * POST /api/v1/attivita-formative/save
   * Salva un'attività formativa
   */
  save(attivitaFormativa: AttivitaFormativeDTO): Observable<AttivitaFormativeDTO | undefined> {
    return this.http
      .post<
        GenericResponse<AttivitaFormativeDTO>
      >(Path.url('/attivita-formative/save'), attivitaFormativa)
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  /**
   * DELETE /api/v1/attivita-formative/{id}
   * Elimina un'attività formativa
   * Nota: implementato ma non utilizzato per ora (gestito dal componente padre)
   */
  delete(id: number, idPiao: number, testoSezione: string): Observable<void> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'attivitaFormativa')
      .set('testoSezione', testoSezione);
    return this.http.delete<void>(Path.url(`/attivita-formative/${id}`), { params });
  }
}
