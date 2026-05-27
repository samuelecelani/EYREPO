import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { map, switchMap } from 'rxjs';
import { RoleRoutingService } from './role-routing.service';
import { AccountService } from './account.service';

@Injectable({
  providedIn: 'root',
})
export class ScaricoMassivoService {
  constructor(private http: HttpClient) {}

  private readonly roleRoutingService = inject(RoleRoutingService);
  private readonly accountService = inject(AccountService);

  scaricaDatiScaricoMassivo(idPiaoList: number[], codicePa: string) {
    return this.accountService.getAccount().pipe(
      switchMap((user) => {
        const isDfp = this.roleRoutingService.isDfpAuthority(user?.typeAuthority);

        let param = new HttpParams().set('userDfp', isDfp.toString());

        if (!isDfp) {
          param = param.set('codicePa', codicePa);
        }

        return this.http
          .post<
            GenericResponse<any>
          >(Path.url(`/notification/excel/generation/batch`), idPiaoList, { params: param })
          .pipe(map((res) => res.data));
      })
    );
  }
}
