import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class OrganoPoliticoService {
  constructor(private http: HttpClient) {}

  /**
   * Elimina un Organo Politico per ID
   * DELETE /organo-politico/{id}
   */
  delete(
    id: number,
    idPiao: number,
    testoSezione: string
  ): Observable<void> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'organiPolitici')
      .set('testoSezione', testoSezione);

    return this.http.delete<void>(Path.url(`/organo-politico/${id}`), { params });
  }
}