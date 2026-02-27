import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Sezione23Service } from './sezione23.service';
import { MonitoraggioPrevenzioneDTO } from '../models/classes/monitoraggio-prevenzione-dto';
import { Path } from '../utils/path';
import { map, switchMap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ModalitaMonitoraggioService {
  private sezione23Service: Sezione23Service = inject(Sezione23Service);
  constructor(private http: HttpClient) {}

  save(modalitaMonitoraggio: MonitoraggioPrevenzioneDTO) {
    return this.http
      .post(Path.url('/monitoraggio-prevenzione/save'), modalitaMonitoraggio)
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }

  delete(id: number) {
    return this.http
      .delete(Path.url(`/monitoraggio-prevenzione/${id}`))
      .pipe(switchMap(() => this.sezione23Service.reloadSezione23AndUpdateSession()));
  }
}
