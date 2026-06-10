import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { Observable } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';
import { AllegatoDTO } from '../models/classes/allegato-dto';

@Injectable({
  providedIn: 'root',
})
export class AttachmentService {
  constructor(private http: HttpClient) {}

  saveAttachment(allegatoDto: AllegatoDTO, file: File): Observable<GenericResponse<AllegatoDTO>> {
    const formData = new FormData();
    formData.append('file', file, allegatoDto.codDocumento);
    formData.append(
      'allegato',
      new Blob([JSON.stringify(allegatoDto)], { type: 'application/json' })
    );

    return this.http.post<GenericResponse<AllegatoDTO>>(Path.url('/allegato/save'), formData, {
      headers: new HttpHeaders({ 'id-spinner': 'default' }),
    });
  }

  getAllAttachmentsByTipologia(
    codTipologia: string[],
    codTipologiaAllegato: string[],
    idPiao: number | undefined,
    isDoc: boolean
  ): Observable<GenericResponse<AllegatoDTO[]>> {
    const params = new HttpParams()
      .set('codTipologia', codTipologia.join(','))
      .set('codTipologiaAllegato', codTipologiaAllegato.join(','))
      .set('idPiao', idPiao ? idPiao : '')
      .set('isDoc', isDoc);

    return this.http.get<GenericResponse<AllegatoDTO[]>>(Path.url('/allegato/by-tipologia'), {
      headers: new HttpHeaders({ 'id-spinner': 'default' }),
      params,
    });
  }

  deleteAttachment(
    idDoc: number,
    fileKey: string,
    isDoc: boolean,
    sezione: string,
    idPiao: number | undefined,
    nameDocumentoFE: string,
    testoSezione: string
  ): Observable<GenericResponse<void>> {
    const params = new HttpParams()
      .set('allegatoId', idDoc)
      .set('fileKey', fileKey)
      .set('isDoc', isDoc)
      .set('codTipologiaFK', sezione)
      .set('idPiao', idPiao ? idPiao : '')
      .set('campiModificati', nameDocumentoFE)
      .set('testoSezione', testoSezione.includes('?') ? testoSezione.split('?')[0] : testoSezione);

    return this.http.delete<GenericResponse<void>>(Path.url('/allegato/delete'), {
      headers: new HttpHeaders({ 'id-spinner': 'default' }),
      params,
    });
  }

  deleteBozza(codDocumento: string): Observable<GenericResponse<void>> {
    const params = new HttpParams().set('codDocumento', codDocumento);
    return this.http.delete<GenericResponse<void>>(Path.url('/allegato/delete-bozza'), {
      params,
    });
  }

  /**
   * Recupera l'URL del PDF generato del PIAO pubblicato (endpoint free, senza autenticazione).
   */
  getPiaoPdfUrl(idPiao: number): Observable<GenericResponse<string>> {
    const params = new HttpParams().set('idPiao', idPiao);
    return this.http.get<GenericResponse<string>>(Path.url('/external/allegato/piao-pdf-url'), {
      params,
    });
  }
}
