import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class ProcedureService {
  constructor(private http: HttpClient) {}

  /**
   * Elimina una Procedura per ID
   * DELETE /procedura/{id}
   */
  delete(
    id: number,
    idPiao: number,
    testoSezione: string
  ): Observable<void> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'procedure')
      .set('testoSezione', testoSezione);

    return this.http.delete<void>(Path.url(`/procedura/${id}`), { params });
  }
}
