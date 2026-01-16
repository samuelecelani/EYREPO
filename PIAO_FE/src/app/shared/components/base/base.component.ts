import { Component, inject, Input, OnDestroy, ViewChild } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { AccountService } from '../../services/account.service';
import { catchError, EMPTY, Observable, of, Subject, switchMap, takeUntil, tap } from 'rxjs';
import { UtenteDTO } from '../../models/classes/utente-dto';
import { ModalBodyComponent } from '../modal/modal-body/modal-body.component';
import { PARiferimentoDTO } from '../../models/classes/pa-riferimento-dto';
import { LoginService } from '../../services/login.service';

@Component({
  selector: 'piao-base',
  imports: [SharedModule],
  templateUrl: './base.component.html',
  styleUrl: './base.component.scss',
})
export class BaseComponent implements OnDestroy {
  @ViewChild('child') child!: ModalBodyComponent;
  isVisible: boolean = false;
  accountService: AccountService = inject(AccountService);
  loginService: LoginService = inject(LoginService);
  private destroy$ = new Subject<void>();
  user!: UtenteDTO | null;
  paRiferimento!: PARiferimentoDTO;

  getVisibility(codFunzionalita: string): Observable<string[] | null> {
    return this.accountService.getAccount().pipe(
      switchMap((user) => {
        const paAttiva = user?.paRiferimento.filter((x) => x.attiva);

        if (!user?.paRiferimento || !paAttiva) {
          //this.loginService.logout();
          return EMPTY;
        }

        this.user = user;
        this.paRiferimento = paAttiva[0];
        const ruoliCode = this.paRiferimento?.ruoli?.map((x) => x.codice) || [];

        return this.accountService.getFunzionalita(ruoliCode);
      }),
      tap((funzionalita) => {
        if (!funzionalita || funzionalita.length <= 0) {
          //this.loginService.logout();
        } else {
          this.isVisible = funzionalita?.includes(codFunzionalita) || false;
        }
      }),
      catchError(() => {
        //this.loginService.logout();
        return of([]);
      }),
      takeUntil(this.destroy$)
    );
  }

  get disabledButtonConfirm(): boolean {
    return this.child?.formGroup?.invalid ?? true;
  }

  ngOnDestroy(): void {
    //emette il valore e pulisce il subject
    this.destroy$.next();
    this.destroy$.complete();
  }
}
