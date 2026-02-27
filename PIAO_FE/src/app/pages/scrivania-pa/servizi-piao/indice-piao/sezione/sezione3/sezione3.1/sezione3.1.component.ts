import { Component, EventEmitter, inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { AccordionComponent } from '../../../../../../../shared/components/accordion/accordion.component';
import { AttachmentComponent } from '../../../../../../../shared/ui/attachment/attachment.component';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { Sezione31DTO } from '../../../../../../../shared/models/classes/sezione-31-dto';
import { INPUT_REGEX } from '../../../../../../../shared/utils/constants';
import { SectionStatusEnum } from '../../../../../../../shared/models/enums/section-status.enum';
import { CodTipologiaAllegatoEnum } from '../../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { CodTipologiaSezioneEnum } from '../../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { areAllValuesNull, hasRequiredErrors } from '../../../../../../../shared/utils/utils';
import { ToastService } from '../../../../../../../shared/services/toast.service';

@Component({
  selector: 'piao-sezione-3-1',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    TextAreaComponent,
    AccordionComponent,
    AttachmentComponent,
  ],
  templateUrl: './sezione3.1.component.html',
  styleUrl: './sezione3.1.component.scss',
})
export class Sezione31Component extends BaseComponent implements OnInit, OnDestroy, ISezioneBase {
  @Input() piaoDTO!: PIAODTO;
  @Input() sezione31Data?: Sezione31DTO;
  @Output() formValueChanged = new EventEmitter<any>();

  isFormReady = false;

  private fb = inject(FormBuilder);
  private toastService = inject(ToastService);

  form!: FormGroup;
  openAccordionIndex: number = 1;

  // Titolo principale
  title: string = 'SEZIONE_31.TITLE';

  // Parte Generale
  subTitleParteGenerale: string = 'SEZIONE_31.PARTE_GENERALE';

  // Accordion 1 - Fotografia dell'organizzazione
  titleAccordion1: string = 'SEZIONE_31.ACCORDION_1.TITLE';

  // Struttura organizzativa
  subtitleStrutturaOrganizzativa: string = 'SEZIONE_31.ACCORDION_1.STRUTTURA_ORG.SUBTITLE';
  labelStrutturaOrganizzativa: string = 'SEZIONE_31.ACCORDION_1.STRUTTURA_ORG.LABEL';

  // Organigramma
  labelOrganigramma: string = 'SEZIONE_31.ACCORDION_1.ORGANIGRAMMA.LABEL';
  descOrganigramma: string = 'SEZIONE_31.ACCORDION_1.ORGANIGRAMMA.DESC';

  // Ampiezza organizzativa
  labelAmpiezzaOrganizzativa: string = 'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.LABEL';

  // Ulteriori dettagli
  labelUlterioriDettagli: string = 'SEZIONE_31.ACCORDION_1.ULTERIORI_DETTAGLI.LABEL';

  // Incarichi dirigenziali
  subtitleIncarichiDirigenziali: string = 'SEZIONE_31.ACCORDION_1.INCARICHI_DIR.SUBTITLE';
  labelIncarichiDirigenziali: string = 'SEZIONE_31.ACCORDION_1.INCARICHI_DIR.LABEL';

  // Profili professionali
  subtitleProfiliProfessionali: string = 'SEZIONE_31.ACCORDION_1.PROFILI_PROF.SUBTITLE';
  labelProfiliProfessionali: string = 'SEZIONE_31.ACCORDION_1.PROFILI_PROF.LABEL';

  // Rappresentazione grafica
  labelRappresentazioneGrafica: string = 'SEZIONE_31.ACCORDION_1.RAPP_GRAFICA.LABEL';

  // Accordion 2 - Linee strategiche
  titleAccordion2: string = 'SEZIONE_31.ACCORDION_2.TITLE';
  labelLineeStrategiche: string = 'SEZIONE_31.ACCORDION_2.LINEE_STRATEGICHE.LABEL';
  descLineeStrategiche: string = 'SEZIONE_31.ACCORDION_2.LINEE_STRATEGICHE.DESC';

  // Parte Funzionale
  subTitleParteFunzionale: string = 'SEZIONE_31.PARTE_FUNZIONALE';
  labelTabellaFunzionale: string = 'SEZIONE_31.TABELLA_FUNZIONALE.LABEL';

  // Configurazione Attachment immagine organigramma
  codTipologia: CodTipologiaSezioneEnum = CodTipologiaSezioneEnum.SEZ3_1;
  codTipologiaAllegatoImage1: CodTipologiaAllegatoEnum =
    CodTipologiaAllegatoEnum.IMMAGINE_SEZIONE_31_1;

  ngOnInit(): void {
    this.createForm();
  }

  getForm(): FormGroup {
    return this.form;
  }

  isFormValid(): boolean {
    return this.form.valid;
  }

  hasFormValues(): boolean {
    return !areAllValuesNull(this.form);
  }

  prepareDataForSave() {
    throw new Error('Method not implemented.');
  }

  validate(): Observable<any> {
    // TODO: Implementare quando disponibile il servizio
    throw new Error('Method not implemented.');
  }

  resetForm(): void {
    this.form.reset();
    this.createForm();
  }

  getSectionStatus(): string {
    // Tutto il form null, DA_COMPILARE
    if (!this.hasFormValues()) {
      return SectionStatusEnum.DA_COMPILARE;
    }

    // Se i required non sono stati compilati, IN_COMPILAZIONE
    if (hasRequiredErrors(this.form)) {
      return SectionStatusEnum.IN_COMPILAZIONE;
    }

    // Form valido e con i campi compilati, COMPILATA
    return SectionStatusEnum.COMPILATA;
  }

  createForm(): void {
    const sezione31 = this.sezione31Data || new Sezione31DTO();

    this.form = this.fb.group({
      statoSezione: this.fb.control<string | null>(sezione31.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Struttura organizzativa al 31.12
      strutturaOrganizzativa: this.fb.control<string | null>(
        sezione31.strutturaOrganizzativa || null,
        [Validators.required, Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      // Ulteriori dettagli ampiezza organizzativa
      ulterioriDettagli: this.fb.control<string | null>(sezione31.ulterioriDettagli || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Incarichi dirigenziali e simili
      incarichiDirigenziali: this.fb.control<string | null>(
        sezione31.incarichiDirigenziali || null,
        [Validators.required, Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      // Rappresentazione dei profili professionali
      profiliProfessionali: this.fb.control<string | null>(sezione31.profiliProfessionali || null, [
        Validators.required,
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Linee strategiche dell'organizzazione
      lineeStrategiche: this.fb.control<string | null>(sezione31.lineeStrategiche || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
    });

    // Emetti il valore del form dopo la creazione
    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  // Handlers per le azioni
  handleCompilaTabella(tipo: string): void {
    // TODO: Implementare apertura modale tabella
    console.log('Compila tabella:', tipo);
    this.toastService.info(`Funzionalità "Compila Tabella" (${tipo}) in sviluppo`);
  }

  handleGeneraGrafico(): void {
    // TODO: Implementare generazione grafico
    console.log('Genera grafico');
    this.toastService.info('Funzionalità "Genera Grafico" in sviluppo');
  }
}
