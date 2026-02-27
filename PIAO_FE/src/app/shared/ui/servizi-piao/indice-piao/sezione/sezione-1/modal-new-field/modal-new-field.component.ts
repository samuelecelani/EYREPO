import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { ModalBodyComponent } from '../../../../../../components/modal/modal-body/modal-body.component';
import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'piao-modal-new-field',
  imports: [SharedModule, TextBoxComponent, ReactiveFormsModule],
  templateUrl: './modal-new-field.component.html',
  styleUrl: './modal-new-field.component.scss',
})
export class ModalNewFieldComponent extends ModalBodyComponent implements OnInit {
  ngOnInit(): void {
    this.formGroup = new FormGroup({
      key: new FormControl<string | null>(null, Validators.required),
    });
  }
}
