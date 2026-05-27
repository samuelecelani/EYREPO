import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { AccountService } from './account.service';

@Injectable({
  providedIn: 'root',
})
export class RoleRoutingService {
  private readonly accountService = inject(AccountService);

  private readonly dfpAuthorities = new Set(['DFP', 'PIAO']);

  isDfpAuthority(typeAuthority?: string | null): boolean {
    if (!typeAuthority) {
      return false;
    }
    return this.dfpAuthorities.has(typeAuthority);
  }

  getHomeRouteByAuthority(typeAuthority?: string | null): string {
    return this.isDfpAuthority(typeAuthority)
      ? '/area-privata-DFP'
      : '/area-privata-PA';
  }

  getHomeRoute$(): Observable<string> {
    return this.accountService
      .getAccount()
      .pipe(map((user) => this.getHomeRouteByAuthority(user?.typeAuthority)));
  }
}
