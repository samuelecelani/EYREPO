import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SvgComponent } from '../svg/svg.component';
import { StrutturaIndicePiaoDTO } from '../../models/classes/struttura-indice-piao-dto';
import { SectionStatusEnum } from '../../models/enums/section-status.enum';
import { SEZIONI_SEMPLIFICATO } from '../../utils/constants';
import { BaseComponent } from '../base/base.component';

@Component({
  selector: 'piao-workflow',
  imports: [SharedModule, SvgComponent],
  templateUrl: './workflow.component.html',
  styleUrl: './workflow.component.scss',
})
export class WorkflowComponent extends BaseComponent implements OnInit, OnChanges {
  @Input() activeStep!: string;

  @Input() steps!: StrutturaIndicePiaoDTO[];

  @Input() isDettaglio: boolean = false;

  @Input() isSemplificato: boolean = false;

  statusCompilated: string[] = [
    SectionStatusEnum.COMPILATA,
    SectionStatusEnum.IN_VALIDAZIONE,
    SectionStatusEnum.VALIDATA,
    SectionStatusEnum.RICHIESTA_APPROVAZIONE,
    SectionStatusEnum.PUBBLICATO,
    SectionStatusEnum.APPROVATO,
  ];

  validStates: string[] = [
    SectionStatusEnum.VALIDATA,
    SectionStatusEnum.RICHIESTA_APPROVAZIONE,
    SectionStatusEnum.PUBBLICATO,
    SectionStatusEnum.APPROVATO,
  ];

  currentStep = 0;
  expandedStepIndex: number | null = null;
  openChild: boolean = false;
  numeroSezioneOpen: string = '';
  mobileExpanded: boolean = false;

  @Output() clickStep: EventEmitter<string> = new EventEmitter<string>();

  ngOnInit(): void {
    this.numeroSezioneOpen = this.activeStep;
    let parentStep = this.activeStep;
    if (parentStep.includes('.')) {
      parentStep = parentStep.slice(0, parentStep.indexOf('.'));
    }
    this.currentStep = Number(parentStep) - 1;
    console.log(this.hasFunzionalita('PIAO_AGGIORNA_APPR_INPUT_DATA'));
    this.steps = this.steps.filter((s) => {
      if (s.numeroSezione === '5') {
        return this.hasFunzionalita('PIAO_AGGIORNA_APPR_INPUT_DATA');
      }
      return true;
    });
    this.updateStep4Availability();
  }

  private updateStep4Availability(): void {
    if (!this.steps || this.steps.length <= 4) return;

    let allOthersValidated: boolean;

    if (this.isSemplificato) {
      const allSteps = this.flattenSteps(this.steps);
      allOthersValidated = SEZIONI_SEMPLIFICATO.every((sezione) =>
        allSteps.some(
          (s) =>
            s.numeroSezione === sezione &&
            this.validStates.includes(s.statoSezione as SectionStatusEnum)
        )
      );
    } else {
      allOthersValidated = this.steps.every(
        (step, index) =>
          index === 4 || this.validStates.includes(step.statoSezione as SectionStatusEnum)
      );
    }

    this.steps[4].disabled = !allOthersValidated;
  }

  private flattenSteps(steps: StrutturaIndicePiaoDTO[]): StrutturaIndicePiaoDTO[] {
    return steps.reduce<StrutturaIndicePiaoDTO[]>((acc, step) => {
      acc.push(step);
      if (step.children?.length) {
        acc.push(...this.flattenSteps(step.children));
      }
      return acc;
    }, []);
  }

  isStepSemplificato(numeroSezione: string): boolean {
    if (this.isSemplificato) {
      if (numeroSezione === '2' || numeroSezione === '3') {
        return !SEZIONI_SEMPLIFICATO.includes(this.numeroSezioneOpen);
      } else {
        return !SEZIONI_SEMPLIFICATO.includes(numeroSezione);
      }
    } else {
      return false;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.ngOnInit();
  }

  handleGoToStep(index: number, step: any, evt: MouseEvent) {
    if (step.children.length === 0) {
      this.currentStep = index;
      this.numeroSezioneOpen = step.numeroSezione;
      this.mobileExpanded = false;
      this.clickStep.emit(step.numeroSezione);
    } else {
      this.handleOpenChild(index, evt);
    }
  }

  handleGoToStepChild(index: number, step: any) {
    this.currentStep = index;
    this.openChild = false;
    this.numeroSezioneOpen = step.numeroSezione;
    this.mobileExpanded = false;
    this.clickStep.emit(step.numeroSezione);
  }

  handleOpenChild(index: number, evt: MouseEvent) {
    if (evt) {
      evt.stopPropagation();
    }

    const step = this.steps[index];
    if (step.disabled || step.children.length === 0) return;

    this.expandedStepIndex = this.expandedStepIndex === index ? null : index;

    this.openChild = this.openChild ? false : true;
  }

  toggleMobilePanel() {
    this.mobileExpanded = !this.mobileExpanded;
  }

  handleMobileStepClick(step: StrutturaIndicePiaoDTO, parentIndex: number) {
    this.currentStep = parentIndex;
    this.numeroSezioneOpen = step.numeroSezione;
    this.mobileExpanded = false;
    this.expandedStepIndex = null;
    this.openChild = false;
    this.clickStep.emit(step.numeroSezione);
  }

  get currentStepLabel(): string {
    const flat = this.flattenSteps(this.steps);
    const found = flat.find((s) => s.numeroSezione === this.numeroSezioneOpen);
    if (found) {
      return `${found.numeroSezione} ${found.testo}`;
    }
    const step = this.steps[this.currentStep];
    if (step) {
      return `${step.numeroSezione} ${step.testo}`;
    }
    return '';
  }

  // Chiudi il pannello child se viene cliccato al di fuori dello step corrente
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    // chiude solo se il click non è dentro un .step che ha il pannello aperto
    if (this.expandedStepIndex !== null) {
      const openStepEl = document.querySelectorAll('.step')[this.expandedStepIndex];
      if (openStepEl && !openStepEl.contains(target)) {
        this.expandedStepIndex = null;
        this.openChild = false;
      }
    }
  }
}
