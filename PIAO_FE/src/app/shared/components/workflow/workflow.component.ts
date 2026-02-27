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

@Component({
  selector: 'piao-workflow',
  imports: [SharedModule, SvgComponent],
  templateUrl: './workflow.component.html',
  styleUrl: './workflow.component.scss',
})
export class WorkflowComponent implements OnInit, OnChanges {
  @Input() activeStep!: string;

  @Input() steps!: StrutturaIndicePiaoDTO[];

  statusCompilated: string[] = [
    SectionStatusEnum.COMPILATA,
    SectionStatusEnum.IN_VALIDAZIONE,
    SectionStatusEnum.VALIDATA,
  ];

  currentStep = 0;
  expandedStepIndex: number | null = null;
  openChild: boolean = false;

  @Output() clickStep: EventEmitter<string> = new EventEmitter<string>();

  ngOnInit(): void {
    if (this.activeStep.includes('.')) {
      this.activeStep = this.activeStep.slice(0, this.activeStep.indexOf('.'));
    }
    this.currentStep = Number(this.activeStep) - 1;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.ngOnInit();
  }

  handleGoToStep(index: number, step: any, evt: MouseEvent) {
    if (step.children.length === 0) {
      this.currentStep = index;
      this.clickStep.emit(step.numeroSezione);
    } else {
      this.handleOpenChild(index, evt);
    }
  }

  handleGoToStepChild(index: number, step: any) {
    this.currentStep = index;
    this.openChild = false;
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
