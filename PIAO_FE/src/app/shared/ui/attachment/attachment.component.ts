import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SvgComponent } from '../../components/svg/svg.component';
import { BaseComponent } from '../../components/base/base.component';
import { ModalAttachmentComponent } from './modal-attachment/modal-attachment.component';
import { ModalComponent } from '../../components/modal/modal.component';
import { AttachmentService } from '../../services/attachment.service';
import { AllegatoDTO } from '../../models/classes/allegato-dto';
import { CodTipologiaAllegatoEnum } from '../../models/enums/cod-tipologia-allegato.enum';
import { CodTipologiaSezioneEnum } from '../../models/enums/cod-tipologia-sezione.enum';
import { map } from 'rxjs';
import { FormArray, FormGroup } from '@angular/forms';
import { TextAreaComponent } from '../../components/text-area/text-area.component';
import { DatePipe, UpperCasePipe } from '@angular/common';
import { SessionStorageService } from '../../services/session-storage.service';
import { KEY_PIAO } from '../../utils/constants';
import { PIAODTO } from '../../models/classes/piao-dto';
import { SectionEnum } from '../../models/enums/section.enum';
import { ModalDeleteComponent } from '../../components/modal-delete/modal-delete.component';

@Component({
  selector: 'piao-attachment',
  imports: [
    SharedModule,
    SvgComponent,
    TextAreaComponent,
    ModalAttachmentComponent,
    ModalComponent,
    ModalDeleteComponent,
    UpperCasePipe,
    DatePipe,
  ],
  templateUrl: './attachment.component.html',
  styleUrl: './attachment.component.scss',
})
export class AttachmentComponent extends BaseComponent implements OnInit {
  @Input() title!: string;
  @Input() classTitle: string = 'title';
  @Input() subTitle!: string;
  @Input() loadAttachment!: string;
  @Input() idPiao!: number;
  @Input() isDoc: boolean = true;
  @Input() form!: FormGroup;
  @Input() showDescription: boolean = false;
  @Input() showName: boolean = false;
  @Input() codTipologia!: CodTipologiaSezioneEnum;
  @Input() codTipologiaAllegato!: CodTipologiaAllegatoEnum;
  @Input() isOnlyAttachment: boolean = false;
  @Input() sezione!: SectionEnum;
  @Input() sezioneObj!: any;
  @Output() attachmentLoaded = new EventEmitter<void>();

  labelTADescAttachment: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.DESC_ATTACHMENT';
  classTADescAttachment: string = 'text-area';

  attachmentService: AttachmentService = inject(AttachmentService);
  sessionStorageService: SessionStorageService = inject(SessionStorageService);

  attachmentArray: AllegatoDTO[] = [];

  fillColor: string = '#0066CC';
  openModal: boolean = false;
  icon: string = 'Attachment';
  iconStyle: string = 'icon-modal';

  isTableAttachment!: boolean;

  isTableAttachmentIndicePiao!: boolean;

  piaoDTO!: PIAODTO;

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  ngOnInit(): void {
    this.isTableAttachment =
      this.codTipologiaAllegato === CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE;

    this.isTableAttachmentIndicePiao = this.codTipologia === CodTipologiaSezioneEnum.PIAO;

    if (this.isOnlyAttachment) {
      this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    }

    this.getAllAttachment();
  }

  getAllAttachment() {
    this.attachmentService
      .getAllAttachmentsByTipologia(
        this.codTipologia,
        this.codTipologiaAllegato,
        this.idPiao,
        this.isDoc
      )
      .pipe(map((res) => res.data))
      .subscribe({
        next: (value: AllegatoDTO[]) => {
          value.forEach((x) => {
            console.log('AllegatoDTO:', x);
            if (this.isOnlyAttachment) {
              this.form.patchValue({ allegati: value });
              // Emetti evento dopo patchValue per notificare il componente padre
              this.attachmentLoaded.emit();
            }
            let splitCodDocumento = x.codDocumento?.split('.') || [];
            x.codDocumentoFE = splitCodDocumento[0];
            x.type = splitCodDocumento[1];
            if (x.codDocumentoFE && x.codDocumentoFE.length > 20) {
              x.codDocumentoFE =
                x.codDocumentoFE.substring(0, 20) + '\n' + x.codDocumentoFE.substring(20);
            }
          });

          this.attachmentArray = value;
        },
        error: (err: any) => {},
      });
  }

  load() {
    this.openModal = true;
  }

  download(downloadUrl: any): void {
    window.open(downloadUrl, '_blank');
  }

  remove(idDoc: number, codDocumento: string) {
    this.attachmentService.deleteAttachment(idDoc, codDocumento, this.isDoc).subscribe({
      next: (value: any) => {
        if (
          this.isOnlyAttachment &&
          ((this.form.controls['allegati'] as FormArray).controls[0] as FormGroup).controls[
            'descrizione'
          ]
        ) {
          const descrizioneControl = (
            (this.form.controls['allegati'] as FormArray).controls[0] as FormGroup
          ).controls['descrizione'];
          descrizioneControl.setValue(null, { emitEvent: true });
          descrizioneControl.markAsPristine();
          descrizioneControl.markAsUntouched();
          descrizioneControl.updateValueAndValidity();

          switch (this.sezione) {
            case SectionEnum.SEZIONE_1:
            case SectionEnum.SEZIONE_2_1:
            case SectionEnum.SEZIONE_2_2:
            case SectionEnum.SEZIONE_2_3:
            case SectionEnum.SEZIONE_3_1:
            case SectionEnum.SEZIONE_3_2:
            case SectionEnum.SEZIONE_3_3_1:
            case SectionEnum.SEZIONE_3_3_2:
            case SectionEnum.SEZIONE_4:
              if (this.sezioneObj?.allegati && this.sezioneObj.allegati[0]) {
                this.sezioneObj.allegati[0].descrizione = undefined;
              }
              break;

            default:
              break;
          }
        }
        this.handleCloseModalDelete();
        this.getAllAttachment();
      },
      error: (err: any) => {},
    });
  }

  downloadLogoImage(allegatoDto: AllegatoDTO): void {
    if (!allegatoDto.base64) {
      return;
    }

    try {
      // Estrai il tipo MIME dalla stringa base64 (es: data:image/png;base64,...)
      const matches = allegatoDto.base64.match(/^data:([^;]+);base64,(.+)$/);
      let mimeType = this.getMimeTypeFromExtension(allegatoDto.type || '');
      let base64Data = allegatoDto.base64;

      if (matches && matches.length === 3) {
        mimeType = matches[1];
        base64Data = matches[2];
      } else if (allegatoDto.base64.startsWith('data:')) {
        // Se ha il prefixo data: ma non matcha il pattern, rimuovilo
        base64Data = allegatoDto.base64.split(',')[1] || allegatoDto.base64;
      }

      // Converti base64 in blob
      const byteCharacters = atob(base64Data);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: mimeType });

      // Determina l'estensione del file
      const extension = mimeType.split('/')[1] || 'png';
      const fileName = `${allegatoDto.codDocumentoFE}.${extension}`;

      // Crea un URL temporaneo e scarica
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Errore durante il download dell'immagine:", error);
    }
  }

  private getMimeTypeFromExtension(extension: string): string {
    // Rimuovi il punto iniziale se presente
    const ext = extension.replace('.', '').toLowerCase();

    const mimeTypes: { [key: string]: string } = {
      png: 'image/png',
      jpg: 'image/jpg',
      jpeg: 'image/jpeg',
      svg: 'image/svg+xml',
      gif: 'image/gif',
      webp: 'image/webp',
      bmp: 'image/bmp',
      ico: 'image/x-icon',
      tiff: 'image/tiff',
      tif: 'image/tiff',
    };

    return mimeTypes[ext] || 'image/png';
  }

  handleCloseModal() {
    this.child.formGroup.reset();
    this.openModal = false;
  }

  handleConfirmModal() {
    console.log(this.child.formGroup.value);

    let allegatoDTO: AllegatoDTO = {
      descrizione: this.child.formGroup.controls['description'].value,
      codDocumento: this.child.formGroup.controls['fileName'].value,
      sizeAllegato: this.child.formGroup.controls['size'].value,
      type: this.child.formGroup.controls['type'].value,
      codTipologiaAllegato: this.codTipologiaAllegato,
      codTipologiaFK: this.codTipologia,
      idEntitaFK: this.idPiao,
      isDoc: this.isDoc,
    };

    this.attachmentService
      .saveAttachment(allegatoDTO, this.child.formGroup.controls['file'].value)
      .subscribe({
        next: (value: any) => {
          this.getAllAttachment();
          this.openModal = false;
          this.child.formGroup.reset();
        },
        error: (err: any) => {
          console.log('value', err);
        },
      });
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }
}
