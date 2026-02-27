import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, switchMap } from 'rxjs';
import { Sezione4Service } from './sezione4.service';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';

@Injectable({
  providedIn: 'root',
})
export class MilestoneService {
  private sezione4Service: Sezione4Service = inject(Sezione4Service);

  constructor(private http: HttpClient) {}

  deleteByIdMilestone(idMilestone: number) {
    return this.http
      .delete(Path.url(`/milestone/${idMilestone}`))
      .pipe(switchMap(() => this.sezione4Service.reloadSezione4AndUpdateSession()));
  }

  saveOrUpdate(milestone: any) {
    return this.http
      .post<GenericResponse<any>>(Path.url(`/milestone/saveOrUpdate`), milestone)
      .pipe(map((res) => res.data));
  }

  // getPromemoriaByMilestone

  // getAllBySottofaseMonitoraggio
}
