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

    console.log(formData);

    return this.http.post<GenericResponse<AllegatoDTO>>(Path.url('/allegato/save'), formData, {
      headers: new HttpHeaders({ 'id-spinner': 'default' }),
    });
  }

  getAllAttachmentsByTipologia(
    codTipologia: string,
    codTipologiaAllegato: string,
    idPiao: number | undefined,
    isDoc: boolean
  ): Observable<GenericResponse<AllegatoDTO[]>> {
    const params = new HttpParams()
      .set('codTipologia', codTipologia)
      .set('codTipologiaAllegato', codTipologiaAllegato)
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
    isDoc: boolean
  ): Observable<GenericResponse<void>> {
    const params = new HttpParams()
      .set('allegatoId', idDoc)
      .set('fileKey', fileKey)
      .set('isDoc', isDoc);

    return this.http.delete<GenericResponse<void>>(Path.url('/allegato/delete'), {
      headers: new HttpHeaders({ 'id-spinner': 'default' }),
      params,
    });
  }
}
