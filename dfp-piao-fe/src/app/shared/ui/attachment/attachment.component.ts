import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SvgComponent } from '../../components/svg/svg.component';
import { BaseComponent } from '../../components/base/base.component';
import { ModalAttachmentComponent } from './modal-attachment/modal-attachment.component';
import { ModalComponent } from '../../components/modal/modal.component';
import { AttachmentService } from '../../services/attachment.service';
import { AllegatoDTO } from '../../models/classes/allegato-dto';
import { CodTipologiaAllegatoEnum } from '../../models/enums/cod-tipologia-allegato.enum';
import { map } from 'rxjs';
import { FormArray, FormGroup } from '@angular/forms';
import { TextAreaComponent } from '../../components/text-area/text-area.component';
import { DatePipe, UpperCasePipe } from '@angular/common';
import { SessionStorageService } from '../../services/session-storage.service';
import { KEY_PIAO } from '../../utils/constants';
import { PIAODTO } from '../../models/classes/piao-dto';
import { SectionEnum } from '../../models/enums/section.enum';
import { ModalDeleteComponent } from '../../components/modal-delete/modal-delete.component';
import JSZip from 'jszip';
import { CommonModule } from '@angular/common';

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
    CommonModule,
  ],
  templateUrl: './attachment.component.html',
  styleUrl: './attachment.component.scss',
})
export class AttachmentComponent extends BaseComponent implements OnInit {
  // TODO: Replace these fallback links with backend fields when available in consultazione APIs.
  private readonly fallbackExternalPiaoUrl = 'https://www.google.com';

  @Input() title!: string;
  @Input() classTitle: string = 'title';
  @Input() subTitle!: string;
  @Input() subTitleDettaglio: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ATTACHMENT.SUB_TITLE_ATTACHMENT_DETTAGLIO';
  @Input() loadAttachment!: string;
  @Input() idPiao!: number;
  @Input() isDoc: boolean = true;
  @Input() form!: FormGroup;
  @Input() showDescription: boolean = false;
  @Input() showName: boolean = false;
  @Input() codTipologia!: SectionEnum[];
  @Input() codTipologiaAllegato!: CodTipologiaAllegatoEnum[];
  @Input() isOnlyAttachment: boolean = false;
  @Input() isRequired: boolean = false;
  @Input() sezione!: SectionEnum;
  @Input() sezioneObj!: any;
  @Input() isdwnAllAttchment: boolean = false;
  @Input() testoSezione: string = '';
  @Input() isPubblicazione: boolean = false;
  @Input() isDettaglio: boolean = false;
  @Input() formName: string = 'allegati';
  @Input() isElencoPiaoPubblicati: boolean = false;
  @Input() isCaricaPiao: boolean = false;
  @Input() externalPiaoLink?: string;
  @Input() zipBaseName?: string;
  @Input() piaoPubblicatoUrl?: string;
  @Output() attachmentLoaded = new EventEmitter<void>();
  @Output() attachmentDeleted = new EventEmitter<void>();

  // Expose FormArray and FormGroup types for template usage
  readonly FormArray = FormArray;
  readonly FormGroup = FormGroup;

  labelTADescAttachment: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.DESC_ATTACHMENT';
  classTADescAttachment: string = 'text-area';

  attachmentService: AttachmentService = inject(AttachmentService);

  attachmentArray: AllegatoDTO[] = [];

  fillColor: string = '#0066CC';
  fillColorDelete: string = '#cc334d';
  openModal: boolean = false;
  icon: string = 'Attachment';
  iconStyle: string = 'icon-modal';

  isTableAttachment!: boolean;

  isTableAttachmentIndicePiao!: boolean;

  isBase64: boolean = false;

  piaoDTO!: PIAODTO;

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  ngOnInit(): void {
    this.isTableAttachment = this.codTipologiaAllegato.includes(
      CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE
    );

    this.isTableAttachmentIndicePiao = this.codTipologia.includes(SectionEnum.PIAO);

    this.isBase64 =
      !this.isElencoPiaoPubblicati &&
      this.codTipologiaAllegato.includes(CodTipologiaAllegatoEnum.IMMAGINE_SEZIONE_31);

    if (this.isOnlyAttachment) {
      this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    }

    this.getAllAttachment();
  }

  getDescrizioneControl() {
    if (!this.form) {
      return null;
    }
    const formArray = this.form.get(this.formName) as FormArray;
    if (!formArray || formArray.length === 0) {
      return null;
    }
    const firstFormGroup = formArray.at(0) as FormGroup;
    return firstFormGroup?.get('descrizione') || null;
  }

  getAllAttachment() {
    // Skip fetching attachments if idPiao is not set (for new PIAO in CARICA mode)
    if (!this.idPiao) {
      this.attachmentArray = [];
      return;
    }

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
          const attachments = value || [];

          attachments.forEach((x) => {
            console.log('AllegatoDTO:', x);
            if (this.isOnlyAttachment || this.isPubblicazione || this.isBase64 || this.isRequired) {
              this.form.patchValue({ [this.formName]: attachments });
              console.log(`Form ${this.formName} patched with:`, attachments);
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

          this.attachmentArray = attachments;
        },
        error: (err: any) => {},
      });
  }

  load() {
    this.openModal = true;
  }

  download(downloadUrl: any, fileName?: string): void {
    console.log('Download URL:', downloadUrl);
    if (!downloadUrl) {
      this.toastService.error('URL di download non disponibile');
      return;
    }

    fetch(downloadUrl, { method: 'GET' })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`Download failed with status ${response.status}`);
        }

        const blob = await response.blob();
        const contentDisposition = response.headers.get('content-disposition');
        const nameFromHeader = this.extractFileNameFromContentDisposition(contentDisposition);
        const safeFileName =
          fileName || nameFromHeader || this.extractFileNameFromUrl(downloadUrl) || 'allegato';

        const objectUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = safeFileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(objectUrl);
      })
      .catch((error) => {
        console.error('Errore durante il download allegato:', error);
        this.toastService.error('Impossibile scaricare il file. Riprova più tardi.');
      });
  }

  private extractFileNameFromContentDisposition(contentDisposition: string | null): string | null {
    if (!contentDisposition) {
      return null;
    }

    const match = contentDisposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i);
    const rawName = match?.[1] || match?.[2];
    return rawName ? decodeURIComponent(rawName) : null;
  }

  private extractFileNameFromUrl(url: string): string | null {
    try {
      const parsedUrl = new URL(url);
      const lastSegment = parsedUrl.pathname.split('/').pop();
      return lastSegment ? decodeURIComponent(lastSegment) : null;
    } catch {
      return null;
    }
  }

  formatFileSize(value?: string): string {
    if (!value) {
      return '0 MB';
    }

    const raw = String(value).trim();
    const normalized = raw.replace(',', '.');
    const explicitUnitMatch = normalized.match(/^(\d+(?:\.\d+)?)\s*(B|KB|MB|GB|TB)$/i);

    if (explicitUnitMatch) {
      const amount = Number(explicitUnitMatch[1]);
      const unit = explicitUnitMatch[2].toUpperCase();
      return `${this.roundSize(amount)} ${unit}`;
    }

    const bytes = Number(normalized);
    if (!Number.isFinite(bytes) || bytes < 0) {
      return '0 MB';
    }

    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    let size = bytes;
    let unitIndex = 0;

    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }

    return `${this.roundSize(size)} ${units[unitIndex]}`;
  }

  private roundSize(value: number): string {
    if (value >= 100) {
      return value.toFixed(0);
    }
    if (value >= 10) {
      return value.toFixed(1);
    }
    return value.toFixed(2);
  }

  get mainPiaoAttachment(): AllegatoDTO | undefined {
    return (
      this.attachmentArray.find((x) => x.codTipologiaAllegato === CodTipologiaAllegatoEnum.PIAO) ??
      this.attachmentArray[0]
    );
  }

  get resolvedMainPiaoAttachment(): AllegatoDTO | undefined {
    if (!this.isElencoPiaoPubblicati) {
      return undefined;
    }

    const piaoAttachment = this.attachmentArray.find((x) => {
      const tipologia = x.codTipologiaAllegato || '';
      return tipologia.includes(CodTipologiaAllegatoEnum.PIAO);
    });

    if (piaoAttachment) {
      return {
        ...piaoAttachment,
        downloadUrl: piaoAttachment.downloadUrl || '',
      };
    }

    return undefined;
  }

  get additionalPiaoAttachments(): AllegatoDTO[] {
    const main = this.resolvedMainPiaoAttachment;
    if (!main) {
      return this.attachmentArray.sort((a, b) => {
        const firstLetterA = ((a.codDocumento || '').match(/[a-zA-Z]/) || [''])[0].toLowerCase();
        const firstLetterB = ((b.codDocumento || '').match(/[a-zA-Z]/) || [''])[0].toLowerCase();
        return firstLetterA.localeCompare(firstLetterB);
      });
    }

    return this.attachmentArray
      .filter((x) => x.id !== main.id)
      .sort((a, b) => {
        const firstLetterA = ((a.codDocumento || '').match(/[a-zA-Z]/) || [''])[0].toLowerCase();
        const firstLetterB = ((b.codDocumento || '').match(/[a-zA-Z]/) || [''])[0].toLowerCase();
        return firstLetterA.localeCompare(firstLetterB);
      });
  }

  get resolvedExternalPiaoLink(): string {
    return this.externalPiaoLink || this.fallbackExternalPiaoUrl;
  }

  redirectToPiaoPubblicato(url: string): void {
    if (!url) {
      this.toastService.error('URL del PIAO pubblicato non disponibile');
      return;
    }
    window.open(url, '_blank');
  }

  remove(idDoc: number, codDocumento: string, nameDocumentoFE: string): void {
    this.attachmentService
      .deleteAttachment(
        idDoc,
        codDocumento,
        this.isDoc,
        this.sezione.toString(),
        this.idPiao,
        nameDocumentoFE,
        this.testoSezione
      )
      .subscribe({
        next: (value: any) => {
          if (
            this.isOnlyAttachment &&
            ((this.form.controls[this.formName] as FormArray).controls[0] as FormGroup).controls[
              'descrizione'
            ]
          ) {
            const descrizioneControl = (
              (this.form.controls[this.formName] as FormArray).controls[0] as FormGroup
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
          if (this.isPubblicazione || this.isBase64 || this.isRequired) {
            this.form.patchValue({ [this.formName]: [] });
            this.attachmentDeleted.emit();
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

    const campiTecnici = this.buildCampiTecnici();

    let allegatoDTO: AllegatoDTO = {
      descrizione: this.child.formGroup.controls['description'].value,
      codDocumento: this.child.formGroup.controls['fileName'].value,
      sizeAllegato: this.child.formGroup.controls['size'].value,
      type: this.child.formGroup.controls['type'].value,
      codTipologiaAllegato: this.codTipologiaAllegato.join(','),
      codTipologiaFK: this.codTipologia.join(','),
      idEntitaFK: this.idPiao,
      isDoc: this.isDoc,
      ...campiTecnici,
    };

    const allegatoRequest = {
      ...allegatoDTO,
      testoSezione: this.testoSezione.includes('?')
        ? this.testoSezione.split('?')[0]
        : this.testoSezione,
      campiModificati: allegatoDTO.codDocumento,
      idPiao: this.idPiao,
    };

    this.attachmentService
      .saveAttachment(allegatoRequest, this.child.formGroup.controls['file'].value)
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

  async downloadAllAttachments(): Promise<void> {
    if (!this.attachmentArray || this.attachmentArray.length === 0) {
      return;
    }

    const zip = new JSZip();

    const downloadPromises = this.attachmentArray.map(async (allegato) => {
      if (!allegato.downloadUrl) return;

      try {
        const response = await fetch(allegato.downloadUrl);
        const blob = await response.blob();
        const fileName = allegato.codDocumento || `file_${allegato.id}`;
        zip.file(fileName, blob);
      } catch (error) {
        console.error(`Errore download ${allegato.codDocumento}:`, error);
      }
    });

    await Promise.all(downloadPromises);

    const zipBlob = await zip.generateAsync({ type: 'blob' });
    const url = window.URL.createObjectURL(zipBlob);
    const link = document.createElement('a');
    link.href = url;
    const safeBase = (this.zipBaseName || `piao_${this.idPiao || 'allegati'}`)
      .replace(/\s+/g, '_')
      .replace(/[^a-zA-Z0-9_-]/g, '');
    link.download = `${safeBase}.zip`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }
}
