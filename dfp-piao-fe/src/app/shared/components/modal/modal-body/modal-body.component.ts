import { Component } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { SharedModule } from '../../../module/shared/shared.module';
import { AttivitaSensibileDTO } from '../../../models/classes/attivita-sensibile-dto';

@Component({
  selector: 'piao-modal-body',
  imports: [SharedModule],
  templateUrl: './modal-body.component.html',
  styleUrl: './modal-body.component.css',
})
export class ModalBodyComponent {
  formGroup!: FormGroup;
}
