import { Component, Input, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { TextAreaComponent } from '../../../components/text-area/text-area.component';
import { INPUT_REGEX } from '../../../utils/constants';
import { SvgComponent } from '../../../components/svg/svg.component';
import { interval, takeWhile, finalize } from 'rxjs';
import { TextBoxComponent } from '../../../components/text-box/text-box.component';

@Component({
  selector: 'piao-modal-attachment',
  imports: [SharedModule, ReactiveFormsModule, TextAreaComponent, TextBoxComponent, SvgComponent],
  templateUrl: './modal-attachment.component.html',
  styleUrl: './modal-attachment.component.scss',
})
export class ModalAttachmentComponent extends ModalBodyComponent implements OnInit {
  @Input() showDescription!: boolean;
  @Input() showName!: boolean;
  @Input() isDoc!: boolean;

  uploadProgress: number = 0;
  fileExtension: string = '';

  labelTADescAttachment: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.DESC_ATTACHMENT_REQUIRED';
  labelTBNameAttachment: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.NAME_ATTACHMENT';
  classTADescAttachment: string = 'text-area';

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      fileName: new FormControl<string | null>(null, Validators.required),
      size: new FormControl<string | null>(null, Validators.required),
      file: new FormControl<File | null>(null, Validators.required),
      description: new FormControl<number | null>(null, [
        Validators.max(100),
        Validators.pattern(INPUT_REGEX),
      ]),
      name: new FormControl<number | null>(null, [
        Validators.max(30),
        Validators.pattern(INPUT_REGEX),
      ]),
    });

    if (this.showName) {
      this.formGroup.controls['name'].addValidators(Validators.required);
    }

    if (this.showDescription) {
      this.formGroup.controls['description'].addValidators(Validators.required);
    }
  }

  onDragOver(event: DragEvent) {
    if (this.formGroup.controls['fileName'].value) {
      event.preventDefault();
    }
  }

  onDrop(event: DragEvent) {
    if (this.formGroup.controls['fileName'].value) {
      event.preventDefault();
      this.handleFile(event.dataTransfer?.files);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.handleFile(input.files);
  }

  private handleFile(files: FileList | null | undefined) {
    if (files && files.length > 0) {
      const file = files[0];

      //Controllo se la dimensione del file supera la dimensione massima
      const maxSize = 20 * 1024 * 1024;
      if (file.size > maxSize) {
        return;
      }

      const fileNameParts = file.name.split('.');
      const baseName = fileNameParts.slice(0, -1).join('.') || fileNameParts[0];
      this.fileExtension = fileNameParts[1];
      this.formGroup.patchValue({
        fileName: this.showName ? this.formGroup.controls['name'].value : baseName,
        size: (file.size / (1024 * 1024)).toFixed(2),
      });

      this.startFakeUploadRx(file);
    }
  }

  startFakeUploadRx(file: File) {
    this.uploadProgress = 0;

    interval(300)
      .pipe(
        takeWhile(() => this.uploadProgress < 100, true),
        finalize(() => {
          this.formGroup.patchValue({
            file,
          });
        })
      )
      .subscribe(() => {
        this.uploadProgress = Math.min(this.uploadProgress + 20, 100);
      });
  }

  removeFile() {
    this.formGroup.controls['file'].reset();
    this.formGroup.controls['fileName'].reset();
    this.formGroup.controls['size'].reset();
    this.uploadProgress = 0;
  }
}
