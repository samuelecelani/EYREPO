import { Directive, inject, Input, OnDestroy, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subject, takeUntil, map, catchError, of } from 'rxjs';
import { AccountService } from '../services/account.service';
import { PARiferimentoDTO } from '../models/classes/pa-riferimento-dto';

/**
 * Structural directive che mostra o nasconde un elemento del template
 * in base alla presenza di un ruolo per l'utente corrente nella PA attiva.
 *
 * Utilizzo con singolo codice:
 *   <div *hasRuolo="'CODICE_RUOLO'">
 *     Contenuto visibile solo se l'utente ha il ruolo
 *   </div>
 *
 * Utilizzo con array di codici (basta che almeno uno sia presente):
 *   <div *hasRuolo="['RUOLO_1', 'RUOLO_2']">
 *     Contenuto visibile se l'utente ha almeno uno dei ruoli
 *   </div>
 *
 * Con else template:
 *   <div *hasRuolo="'CODICE_RUOLO'; else noAccess">
 *     Contenuto visibile
 *   </div>
 *   <ng-template #noAccess>
 *     <p>Non hai il ruolo necessario.</p>
 *   </ng-template>
 */
@Directive({
  selector: '[hasRuolo]',
  standalone: true,
})
export class HasRuoloDirective implements OnDestroy {
  private readonly accountService = inject(AccountService);
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);

  private readonly destroy$ = new Subject<void>();
  private hasView = false;
  private elseTemplateRef: TemplateRef<unknown> | null = null;

  /** Template da mostrare quando il ruolo NON è presente */
  @Input()
  set hasRuoloElse(templateRef: TemplateRef<unknown> | null) {
    this.elseTemplateRef = templateRef;
    this.updateView(this.lastResult);
  }

  private lastResult = false;

  /**
   * Riceve il codice (o array di codici) ruolo e verifica se l'utente corrente
   * possiede almeno uno dei ruoli nella PA attiva, mostrando o nascondendo l'elemento.
   */
  @Input()
  set hasRuolo(codRuolo: string | string[]) {
    const codici = Array.isArray(codRuolo) ? codRuolo : [codRuolo];

    if (!codici.length || codici.every((c) => !c)) {
      this.updateView(false);
      return;
    }

    this.accountService
      .getPaAttiva$()
      .pipe(
        map((paAttiva: PARiferimentoDTO) => {
          const ruoli = paAttiva?.ruoli || [];
          return ruoli.some((r) => codici.includes(r.codice));
        }),
        catchError(() => of(false)),
        takeUntil(this.destroy$)
      )
      .subscribe((hasRole) => {
        this.lastResult = hasRole;
        this.updateView(hasRole);
      });
  }

  /**
   * Aggiorna la vista: mostra il template principale se autorizzato,
   * altrimenti mostra l'else template (se fornito) o svuota il container.
   */
  private updateView(show: boolean): void {
    if (show && !this.hasView) {
      this.viewContainer.clear();
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!show && this.hasView) {
      this.viewContainer.clear();
      if (this.elseTemplateRef) {
        this.viewContainer.createEmbeddedView(this.elseTemplateRef);
      }
      this.hasView = false;
    } else if (!show && !this.hasView) {
      this.viewContainer.clear();
      if (this.elseTemplateRef) {
        this.viewContainer.createEmbeddedView(this.elseTemplateRef);
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
