import { Component, Input, OnInit, ViewChild, ElementRef, inject } from '@angular/core';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { TextAreaComponent } from '../../../components/text-area/text-area.component';
import { INPUT_REGEX } from '../../../utils/constants';
import { SvgComponent } from '../../../components/svg/svg.component';
import { interval, takeWhile, finalize } from 'rxjs';
import { TextBoxComponent } from '../../../components/text-box/text-box.component';
import { Toast } from '../../../models/interfaces/toast';
import { ToastService } from '../../../services/toast.service';

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
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  toastService: ToastService = inject(ToastService);

  uploadProgress: number = 0;
  fileExtension: string = '';

  labelTADescAttachment: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.DESC_ATTACHMENT_SHORT';
  labelTBNameAttachment: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.NAME_ATTACHMENT';
  classTADescAttachment: string = 'text-area';

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      fileName: new FormControl<string | null>(null, Validators.required),
      size: new FormControl<string | null>(null, Validators.required),
      type: new FormControl<string | null>(null),
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

    if (this.showDescription && this.isDoc) {
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
    // Resetta il valore dell'input per permettere di selezionare lo stesso file
    input.value = '';
  }

  private handleFile(files: FileList | null | undefined) {
    if (files && files.length > 0) {
      const file = files[0];

      //Controllo se la dimensione del file supera la dimensione massima
      const maxSize = 20 * 1024 * 1024;
      if (file.size > maxSize) {
        this.toastService.error(
          'Dimensioni allegato non valido: ' +
            (file.size / (1024 * 1024)).toFixed(2) +
            'MB. Massimo consentito: 20MB'
        );
        return;
      }

      // Se non è un documento (è un'immagine), controlla le dimensioni
      if (!this.isDoc) {
        const reader = new FileReader();
        reader.onload = (e: ProgressEvent<FileReader>) => {
          const img = new Image();
          img.onload = () => {
            // Verifica che l'immagine sia esattamente 800x400 px
            if (img.width > 800 || img.height > 400) {
              this.toastService.error(
                'Dimensioni immagine non valide: ' +
                  img.width +
                  'x' +
                  img.height +
                  '. Richiesto: 800x400'
              );

              return;
            }

            // Se le dimensioni sono corrette, procedi con il caricamento
            this.processFile(file);
          };
          img.src = e.target?.result as string;
        };
        reader.readAsDataURL(file);
      } else {
        // Se è un documento, procedi direttamente
        this.processFile(file);
      }
    }
  }

  private processFile(file: File) {
    const fileNameParts = file.name.split('.');
    const baseName = fileNameParts.slice(0, -1).join('.') || fileNameParts[0];
    this.fileExtension = '.' + fileNameParts[1];
    this.formGroup.patchValue({
      fileName: this.showName ? this.formGroup.controls['name'].value : baseName,
      size: (file.size / (1024 * 1024)).toFixed(2),
      type: this.fileExtension,
    });

    this.startFakeUploadRx(file);
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
    // Resetta il valore dell'input file
    if (this.fileInput && this.fileInput.nativeElement) {
      this.fileInput.nativeElement.value = '';
    }
  }
}
