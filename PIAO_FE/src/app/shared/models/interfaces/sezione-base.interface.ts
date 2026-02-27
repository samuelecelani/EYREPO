import { FormGroup } from '@angular/forms';
import { Observable } from 'rxjs';

/**
 * Interfaccia base che tutte le sezioni devono implementare
 * per permettere al componente padre di gestirle in modo uniforme
 */
export interface ISezioneBase {
  /**
   * Ritorna il form della sezione
   */
  getForm(): FormGroup;

  /**
   * Verifica se il form è valido
   */
  isFormValid(): boolean;

  /**
   * Verifica se il form ha dei valori (non è completamente vuoto)
   */
  hasFormValues(): boolean;

  /**
   * Prepara i dati del form per il salvataggio
   * @returns I dati formattati pronti per essere inviati al backend
   */
  prepareDataForSave(): any;

  /**
   * Esegue la validazione della sezione
   * @returns Observable che emette quando la validazione è completata
   */
  validate(): Observable<any>;

  /**
   * Resetta il form ai valori iniziali
   */
  resetForm(): void;

  /**
   * Ritorna lo stato attuale della sezione (da compilare, in compilazione, compilata, ecc.)
   */
  getSectionStatus(): string;

  /**
   * Crea il form della sezione
   *  */
  createForm(): void;

  /**
   * Indica se il form è stato creato e la sezione è pronta per il rendering
   */
  isFormReady: boolean;
}
