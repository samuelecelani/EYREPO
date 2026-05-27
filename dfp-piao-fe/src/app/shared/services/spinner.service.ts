
// spinner.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SpinnerService {
  private counters = new Map<string, number>();
  private loadingSubject = new BehaviorSubject<boolean>(false);

  /** Osservato dal componente */
  loading$ = this.loadingSubject.asObservable();

  addLoading(id: string): void {
    const current = this.counters.get(id) ?? 0;
    this.counters.set(id, current + 1);
    this.updateGlobalState();
  }

  removeLoading(id: string): void {
    const current = this.counters.get(id) ?? 0;
    const next = Math.max(0, current - 1);
    if (next === 0) this.counters.delete(id);
    else this.counters.set(id, next);
    this.updateGlobalState();
  }

  /** Mostra lo spinner se c’è almeno una richiesta attiva */
  private updateGlobalState(): void {
    const isLoading = this.counters.size > 0;
    this.loadingSubject.next(isLoading);
  }
}
