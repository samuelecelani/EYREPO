import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class RisorseFinanziarieService {
  constructor(private http: HttpClient) {}

  /**
   * Elimina una Risorsa Finanziaria per ID
   * DELETE /risorse-finanziarie/{id}
   */
  delete(
    id: number,
    idPiao: number,
    testoSezione: string
  ): Observable<void> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'risorseFinanziarie')
      .set('testoSezione', testoSezione);

    return this.http.delete<void>(Path.url(`/ovp-risorse-finanziarie/${id}`), { params });
  }
}
