import { inject, Injectable } from '@angular/core';
import { EventoRischiosoDTO } from '../models/classes/evento-rischioso-dto';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Sezione23Service } from './sezione23.service';
import { map, switchMap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class EventoRischioService {
  private sezione23Service = inject(Sezione23Service);

  constructor(private http: HttpClient) {}

  save(eventoRischioso: EventoRischiosoDTO) {
    return this.http
      .post<GenericResponse<EventoRischiosoDTO>>(Path.url(`/evento-rischio/save`), eventoRischioso)
      .pipe(map((res) => res.data));
  }

  delete(id: number, idPiao: number, testoSezione: string) {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('testoSezione', testoSezione)
      .set('campiModificati', 'eventiRischio');
    return this.http
      .delete(Path.url(`/evento-rischio/${id}`), { params })
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  getEventiRischiosiByIdAttivitaSensibile(idAttivitaSensibile: number) {
    return this.http
      .get<
        GenericResponse<EventoRischiosoDTO[]>
      >(Path.url(`/evento-rischio/attivita-sensibile/${idAttivitaSensibile}`))
      .pipe(map((response) => response.data || []));
  }

  deleteByAttivitaSensibile(idAttivitaSensibile: number, idPiao: number, testoSezione: string) {
    const params = new HttpParams()
      .set('idPiao', idPiao.toString())
      .set('testoSezione', testoSezione)
      .set('campiModificati', 'eventiRischioAttivitaSensibile');
    return this.http
      .delete(Path.url(`/evento-rischio/attivita-sensibile/${idAttivitaSensibile}`), { params })
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
