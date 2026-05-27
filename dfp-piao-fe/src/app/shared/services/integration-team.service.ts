import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class IntegrationTeamService {
  constructor(private http: HttpClient) {}

  /**
   * Elimina un Integration Team per ID
   * DELETE /integration-team/{id}
   */
  delete(
    id: number,
    idPiao: number,
    testoSezione: string
  ): Observable<void> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'integrationTeams')
      .set('testoSezione', testoSezione);

    return this.http.delete<void>(Path.url(`/integration-team/${id}`), { params });
  }
}
