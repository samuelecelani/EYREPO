import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class FondiEuropeiService {
  constructor(private http: HttpClient) {}

  /**
   * Elimina un Fondo Europeo per ID
   * DELETE /fondi-europei/{id}
   */
  delete(
    id: number,
    idPiao: number,
    testoSezione: string
  ): Observable<void> {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('campiModificati', 'fondiEuropei')
      .set('testoSezione', testoSezione);

    return this.http.delete<void>(Path.url(`/fondi-europei/${id}`), { params });
  }
}
