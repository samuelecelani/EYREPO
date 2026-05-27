import { inject, Injectable, signal } from '@angular/core';
import { NgxPermissionsService } from 'ngx-permissions';
import { UtenteDTO } from '../models/classes/utente-dto';
import { PARiferimentoDTO } from '../models/classes/pa-riferimento-dto';
import { RuoloUtenteDTO } from '../models/classes/ruolo-utente-dto';

export interface ProfilePaRiferimento {
  user: UtenteDTO;
  pa: PARiferimentoDTO;
}

const SESSION_PA_KEY = 'selectedPaCode';

@Injectable({ providedIn: 'root' })
export class UserSessionService {
  private readonly permissionsService = inject(NgxPermissionsService);

  private readonly _user = signal<UtenteDTO | null>(null);
  private readonly _selectedPaRiferimento = signal<PARiferimentoDTO | null>(null);
  private readonly _needsSelection = signal<boolean>(false);
  private _selectionModalOpen = false;

  readonly user = this._user.asReadonly();
  readonly selectedPaRiferimento = this._selectedPaRiferimento.asReadonly();
  readonly needsSelection = this._needsSelection.asReadonly();

  /**
   * Carica l'utente ricevuto dall'API.
   * Se esiste una sola PA la seleziona automaticamente.
   * Se viene passato un preferredPaCode (recuperato dallo stato OIDC dopo re-login),
   * lo cerca tra le opzioni e lo seleziona senza mostrare la modale.
   * Se una PA ha attiva === true, la seleziona automaticamente.
   * Altrimenti imposta needsSelection = true.
   */
  setUser(user: UtenteDTO, preferredPaCode?: string): void {
    this._user.set(user);

    // Utente DFP: typeAuthority sul profilo, nessuna PA
    if (user.typeAuthority === 'PIAO' && (!user.paRiferimento || user.paRiferimento.length === 0)) {
      this._applySelection(null);
      return;
    }

    const allPa = user.paRiferimento || [];
    if (allPa.length === 1) {
      this._applySelection(allPa[0]);
      return;
    }
    if (allPa.length > 1) {
      // Cerca la PA già attiva
      const attiva = allPa.find((pa) => pa.attiva);
      if (attiva) {
        this._applySelection(attiva);
        return;
      }

      // Controlla sessionStorage o preferredPaCode (sopravvive a F5 e redirect OAuth stesso tab)
      const savedPaCode = sessionStorage.getItem(SESSION_PA_KEY) ?? preferredPaCode;
      if (savedPaCode) {
        const match = allPa.find((pa) => pa.codePA === savedPaCode);
        if (match) {
          this._applySelection(match);
          return;
        }
      }
      this._needsSelection.set(true);
    }
  }

  /** Restituisce tutte le coppie (utente, PA) disponibili */
  getAllOptions(): ProfilePaRiferimento[] {
    const user = this._user();
    if (!user) return [];
    return (user.paRiferimento || []).map((pa) => ({ user, pa }));
  }

  selectPaRiferimento(pa: PARiferimentoDTO): void {
    this._applySelection(pa);
  }

  private _applySelection(pa: PARiferimentoDTO | null): void {
    let permissions: string[];
    if (pa) {
      const ruoli = pa.ruoli || [];
      permissions = ruoli.map((r) => r.codice).filter(Boolean);
    } else {
      // Utente DFP: typeAuthority direttamente sull'utente
      const user = this._user();
      permissions = user?.typeAuthority ? [user.typeAuthority] : [];
    }
    this.permissionsService.loadPermissions(permissions);

    this._selectedPaRiferimento.set(pa);
    this._needsSelection.set(false);
    if (pa) {
      sessionStorage.setItem(SESSION_PA_KEY, pa.codePA);
    }
  }

  getSelectedUser(): UtenteDTO | null {
    return this._user();
  }

  getSelectedPaRiferimento(): PARiferimentoDTO | null {
    return this._selectedPaRiferimento();
  }

  getRuoli(): RuoloUtenteDTO[] {
    const pa = this._selectedPaRiferimento();
    if (pa) {
      return pa.ruoli || [];
    }
    return [];
  }

  hasRuolo(codice: string): boolean {
    return this.getRuoli().some((r) => r.codice === codice);
  }

  hasTypology(typology: string): boolean {
    const user = this._user();
    return user?.typeAuthority === typology;
  }

  hasSelectedPaRiferimento(): boolean {
    return this._selectedPaRiferimento() !== null;
  }

  isSelectionModalOpen(): boolean {
    return this._selectionModalOpen;
  }

  markSelectionModalOpen(): void {
    this._selectionModalOpen = true;
  }

  markSelectionModalClosed(): void {
    this._selectionModalOpen = false;
  }

  clear(): void {
    this._user.set(null);
    this._selectedPaRiferimento.set(null);
    this._needsSelection.set(false);
    this._selectionModalOpen = false;
    sessionStorage.removeItem(SESSION_PA_KEY);
    this.permissionsService.flushPermissions();
  }
}
