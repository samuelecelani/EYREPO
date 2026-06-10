import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Path } from '../utils/path';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class PrincipiGuidaService {
  constructor(private http: HttpClient) {}

  /**
   * Elimina un Principio guida esistente
   * DELETE /principi-guida/{id}
   */
  delete(id: number, idPiao: number, testoSezione: string): Observable<void> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('testoSezione', testoSezione)
      .set('campiModificati', 'principiGuida');
    return this.http.delete<void>(Path.url(`/principio-guida/${id}`), { params });
  }
}
