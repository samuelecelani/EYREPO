import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX } from '../../../../../../../utils/constants';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { AmpiezzaOrganizzativaDTO } from '../../../../../../../models/classes/ampiezza-organizzativa-dto';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TextBoxComponent } from '../../../../../../../components/text-box/text-box.component';

@Component({
  selector: 'piao-modal-ampiezza-organizzativa',
  imports: [SharedModule, TextBoxComponent, ReactiveFormsModule],
  templateUrl: './modal-ampiezza-organizzativa.component.html',
  styleUrl: './modal-ampiezza-organizzativa.component.scss',
})
export class ModalAmpiezzaOrganizzativaComponent extends ModalBodyComponent implements OnInit {
  @Input() ampiezzaToEdit!: AmpiezzaOrganizzativaDTO;
  @Input() piaoDTO!: PIAODTO;
  @Input() lenghtArray!: number;

  title: string = 'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.MODAL.TITLE';
  subTitle: string = 'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.MODAL.SUB_TITLE';
  labelUnitaOrganizzativa: string =
    'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.MODAL.UNITA_ORGANIZZATIVA_LABEL';
  labelNRisorseUmane: string = 'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.MODAL.N_RISORSE_UMANE_LABEL';

  main: string = 'main';

  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.ampiezzaToEdit?.id || null],
      idSezione31: [this.ampiezzaToEdit?.idSezione31 || this.piaoDTO.idSezione31 || null],
      unitaOrganizzativa: [
        this.ampiezzaToEdit?.unitaOrganizzativa || null,
        [Validators.maxLength(200), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      numRisorseUmane: [
        this.ampiezzaToEdit?.numRisorseUmane || null,
        [Validators.maxLength(50), Validators.pattern(ONLY_NUMBERS_REGEX), Validators.required],
      ],
    });
  }
}
