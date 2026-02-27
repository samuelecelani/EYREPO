import { AdempimentoNormativoDTO } from './../../../../../../../models/classes/adempimento-normativo-dto';
import { Component, inject, Input, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { TextAreaComponent } from '../../../../../../../components/text-area/text-area.component';
import { INPUT_REGEX } from '../../../../../../../utils/constants';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
@Component({
  selector: 'piao-modal-elenco-adempimento-normativo',
  imports: [SharedModule, TextAreaComponent, ReactiveFormsModule],
  templateUrl: './modal-adempimento-normativo.component.html',
  styleUrl: './modal-adempimento-normativo.component.scss',
})
export class ModalAdempimentoNormativoComponent extends ModalBodyComponent implements OnInit {
  @Input() adempimentoNormativoToEdit!: AdempimentoNormativoDTO;
  @Input() piaoDTO!: PIAODTO;

  title: string = 'SEZIONE_23.ELENCO_AZIONI.MODAL.TITLE';
  subTitle: string = 'SEZIONE_23.ELENCO_AZIONI.MODAL.SUB_TITLE';
  labelNormativa: string = 'SEZIONE_23.ELENCO_AZIONI.MODAL.NORMATIVA_LABEL';
  labelAzionePrevista: string = 'SEZIONE_23.ELENCO_AZIONI.MODAL.AZIONE_PREVISTA_LABEL';

  main: string = 'main';

  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.adempimentoNormativoToEdit?.id || null],
      idSezione23: [
        this.adempimentoNormativoToEdit?.idSezione23 || this.piaoDTO.idSezione23 || null,
      ],
      normativa: [
        this.adempimentoNormativoToEdit?.normativa || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      azione: [
        this.adempimentoNormativoToEdit?.azione || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
    });
  }

  handleAddModalOpened() {
    this.main = 'main-double-modal';
  }

  handleAddModalClosed() {
    this.main = 'main';
  }
}
