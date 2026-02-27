import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { UtenteRuoloPaDTO } from '../models/classes/utente-ruolo-pa-dto';
import { PARiferimentoDTO } from '../models/classes/pa-riferimento-dto';
import { SelectedUserState } from '../models/interfaces/SelectedUserState';

@Injectable({
  providedIn: 'root',
})
export class GestionaleStateService {
  private selectedUserSubject = new BehaviorSubject<SelectedUserState | null>(null);
  public selectedUser$ = this.selectedUserSubject.asObservable();

  setSelectedUser(user: UtenteRuoloPaDTO | null, paRiferimento?: PARiferimentoDTO): void {
    if (user && paRiferimento) {
      this.selectedUserSubject.next({ user, paRiferimento });
    } else {
      this.selectedUserSubject.next(null);
    }
  }

  getSelectedUser(): SelectedUserState | null {
    return this.selectedUserSubject.value;
  }
}
