import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { MONUMENT_ICON, PROFILE_ICON } from '../../../shared/utils/constants';
import { ProfiloUtenteDTO } from '../../../shared/models/classes/profilo-utente-dto';
import { ModalModifyInfoProfiloComponent } from '../../../shared/components/modal-edit-profile/modal-modify-info-profilo/modal-modify-info-profilo.component';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { Funzione } from '../../../shared/models/classes/funzione';
import { take, takeUntil } from 'rxjs';
import { SvgComponent } from '../../../shared/components/svg/svg.component';
import { LabelValue } from '../../../shared/models/interfaces/label-value';
import { ModalSelezionaPaComponent } from '../../../shared/ui/utente/modal-seleziona-pa/modal-seleziona-pa.component';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-profilo',
  imports: [
    CommonModule,
    TranslateModule,
    ModalModifyInfoProfiloComponent,
    ModalComponent,
    ButtonComponent,
    SvgComponent,
    ModalSelezionaPaComponent,
  ],
  templateUrl: './profilo.component.html',
  styleUrl: './profilo.component.scss',
})
export class ProfiloComponent extends BaseComponent implements OnInit {
  showEditProfileModal = false;
  iconProfile: string = PROFILE_ICON;

  iconSelectPA: string = MONUMENT_ICON;

  openModalSelectPA = false;

  @Input() form!: FormGroup;

  // radioPaOptions: LabelValue[] = [
  //   { label: 'label1', value: 'value1', additionalField: true },
  //   { label: 'label2', value: 'value2', additionalField: false },
  //   { label: 'label3', value: 'value3', additionalField: false },
  // ];

  radioPaOptions: LabelValue[] = [];

  profiloData: ProfiloUtenteDTO = {
    qualifica: '',
    telefono: '',
    email: '',
  };

  profiloUtente: string = '';

  denominazionePA: string = '';

  ngOnInit(): void {
    this.getUserContext$()
      .pipe(take(1))
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ user, paRiferimento }) => {
        this.user = user;
        this.paRiferimento = paRiferimento;
        this.profiloData = {
          ...this.profiloData,
          email: paRiferimento.email ?? this.profiloData.email,
          telefono: paRiferimento.numeroTelefono ?? this.profiloData.telefono,
          qualifica: paRiferimento.qualifica ?? this.profiloData.qualifica,
        };
        //set denominazionePA && profiloUtente
        this.denominazionePA = paRiferimento.denominazionePA ?? '';
        const ruoloAttivo = this.paRiferimento?.ruoli?.find((r) => r.ruoloAttivo);
        this.profiloUtente = ruoloAttivo?.descrizione ?? '';

        this.populateRadioPaOptions();
      });
  }

  populateRadioPaOptions() {
    if (this.user && this.user.paRiferimento) {
      // La PA "attiva" mostrata come selezionata nella modale è quella
      // visualizzata sul profilo (cambio locale/fake, non a livello globale).
      // Fallback su pa.attiva solo al primo caricamento, finché denominazionePA non è valorizzata.
      this.radioPaOptions = this.user.paRiferimento.map((pa) => ({
        label: pa.denominazionePA,
        value: pa.denominazionePA,
        additionalField: this.denominazionePA
          ? pa.denominazionePA === this.denominazionePA
          : pa.attiva,
      }));
    } else {
      this.radioPaOptions = [];
    }
  }

  openEditProfileModal(): void {
    this.showEditProfileModal = true;
  }

  closeEditProfileModal(): void {
    this.showEditProfileModal = false;
  }

  saveProfileData(): void {
    if (this.child && this.child.formGroup) {
      const datiAggiornati: ProfiloUtenteDTO = {
        qualifica: this.child.formGroup.get('qualifica')?.value || '',
        telefono: this.child.formGroup.get('telefono')?.value || '',
        email: this.child.formGroup.get('email')?.value || '',
      };

      this.profiloData = datiAggiornati;
      this.closeEditProfileModal();
    }
  }

  savePaData(): void {
    this.getUserContext$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ user }) => {
        const selectedPa = this.child?.formGroup.get('radioPaOptions')?.value;
        if (user) {
          let paRiferimento =
            user.paRiferimento.find((pa) => pa.denominazionePA === selectedPa) ||
            user.paRiferimento[0];
          this.denominazionePA = paRiferimento.denominazionePA;

          this.profiloData = {
            ...this.profiloData,
            qualifica: paRiferimento.qualifica ?? this.profiloData.qualifica,
          };

          this.profiloUtente =
            paRiferimento.ruoli?.find((r) => r.ruoloAttivo)?.descrizione ?? this.profiloUtente;

          // Cambio "fake" solo a livello di pagina: aggiorna le opzioni della modale
          // in modo che la PA pre-selezionata alla riapertura sia quella scelta qui,
          // senza modificare lo stato globale dell'applicazione.
          this.populateRadioPaOptions();
        }
        this.openModalSelectPA = false;
      });
  }

  onModalDataSaved(data: ProfiloUtenteDTO): void {
    this.profiloData = data;
    this.closeEditProfileModal();
  }

  openModalSelezionaAmministrazione(): void {
    this.openModalSelectPA = true;
  }

  funzioni: Funzione[] = [
    {
      id: 1,
      titolo: 'Attività di validazione',
      descrizione:
        'Validazione dei contenuti dalle aree tematiche dei documenti relativi al ciclo della performance della propria PA.',
      expanded: true,
    },
    {
      id: 2,
      titolo: 'Attività di modifica PIAO',
      descrizione:
        "Modifica e gestione dei documenti relativi alla performance dell'organizzazione.",
      expanded: false,
    },
    {
      id: 3,
      titolo: 'Attività di consultazione',
      descrizione: 'Consultazione dei documenti e delle informazioni relative alla performance.',
      expanded: false,
    },
    {
      id: 4,
      titolo: 'Attività di estrazione dati',
      descrizione: 'Estrazione e download dei dati relativi alla performance in vari formati.',
      expanded: false,
    },
  ];

  toggleFunzione(funzione: Funzione): void {
    funzione.expanded = !funzione.expanded;
  }
}
