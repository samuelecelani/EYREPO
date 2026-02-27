import { Directive, inject, Input, OnDestroy, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subject, takeUntil, of, catchError } from 'rxjs';
import { AccountService } from '../services/account.service';

/**
 * Structural directive che mostra o nasconde un elemento del template
 * in base alla presenza di una funzionalità (ruolo) per l'utente corrente.
 *
 * Utilizzo con singolo codice:
 *   <div *hasFunzionalita="'CODICE_FUNZIONALITA'">
 *     Contenuto visibile solo se l'utente ha la funzionalità
 *   </div>
 *
 * Utilizzo con array di codici (basta che almeno uno sia presente):
 *   <div *hasFunzionalita="['CODICE_1', 'CODICE_2']">
 *     Contenuto visibile se l'utente ha almeno una delle funzionalità
 *   </div>
 *
 * Con else template:
 *   <div *hasFunzionalita="'CODICE_FUNZIONALITA'; else noAccess">
 *     Contenuto visibile
 *   </div>
 *   <ng-template #noAccess>
 *     <p>Non hai i permessi per visualizzare questo contenuto.</p>
 *   </ng-template>
 */
@Directive({
  selector: '[hasFunzionalita]',
  standalone: true,
})
export class HasFunzionalitaDirective implements OnDestroy {
  private readonly accountService = inject(AccountService);
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);

  private readonly destroy$ = new Subject<void>();
  private hasView = false;
  private elseTemplateRef: TemplateRef<unknown> | null = null;

  /** Template da mostrare quando la funzionalità NON è presente */
  @Input()
  set hasFunzionalitaElse(templateRef: TemplateRef<unknown> | null) {
    this.elseTemplateRef = templateRef;
    this.updateView(this.lastResult);
  }

  private lastResult = false;

  /**
   * Riceve il codice (o array di codici) funzionalità e verifica se l'utente corrente
   * dispone di almeno uno dei permessi corrispondenti, mostrando o nascondendo l'elemento.
   */
  @Input()
  set hasFunzionalita(codFunzionalita: string | string[]) {
    const codici = Array.isArray(codFunzionalita) ? codFunzionalita : [codFunzionalita];

    if (!codici.length || codici.every((c) => !c)) {
      this.updateView(false);
      return;
    }

    this.accountService
      .getFunzionalitaUtente$()
      .pipe(
        catchError(() => of([] as string[])),
        takeUntil(this.destroy$)
      )
      .subscribe((funzionalita) => {
        const hasPerm = codici.some((cod) => funzionalita.includes(cod));
        this.lastResult = hasPerm;
        this.updateView(hasPerm);
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
      // Caso iniziale: assicura che l'else template sia presente
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
