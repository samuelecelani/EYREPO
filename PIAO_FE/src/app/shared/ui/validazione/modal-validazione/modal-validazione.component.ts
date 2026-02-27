import { Component, inject, Input, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { FormBuilder, Validators } from '@angular/forms';
import { TextAreaComponent } from '../../../components/text-area/text-area.component';
import { CardAlertComponent } from '../../card-alert/card-alert.component';
import { INPUT_REGEX, WARNING_ICON } from '../../../utils/constants';

@Component({
  selector: 'piao-modal-validazione',
  imports: [SharedModule, TextAreaComponent, CardAlertComponent],
  templateUrl: './modal-validazione.component.html',
  styleUrls: ['./modal-validazione.component.scss'],
})
export class ModalValidazioneComponent extends ModalBodyComponent implements OnInit {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() subTitleParams: Record<string, string> = {};
  @Input() infoOperazione!: string;
  @Input() isOsservazioni!: boolean;

  iconAlert: string = WARNING_ICON;

  deadline!: string;
  labelOsservazioni: string = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.OSSERVAZIONI_LABEL';

  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    if (this.isOsservazioni) {
      this.deadline = `31/12/${new Date().getFullYear()}`;
      this.formGroup = this.fb.group({
        osservazioni: [
          null,
          [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
        ],
      });
    }
  }
}
