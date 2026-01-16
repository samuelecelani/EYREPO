import { Component, inject, Input, OnInit } from '@angular/core';
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
import { FormGroup } from '@angular/forms';
import { TextAreaComponent } from '../../components/text-area/text-area.component';

@Component({
  selector: 'piao-attachment',
  imports: [
    SharedModule,
    SvgComponent,
    TextAreaComponent,
    ModalAttachmentComponent,
    ModalComponent,
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

  labelTADescAttachment: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.DESC_ATTACHMENT';
  classTADescAttachment: string = 'text-area';

  attachmentService: AttachmentService = inject(AttachmentService);

  attachmentArray: AllegatoDTO[] = [];

  fillColor: string = '#0066CC';
  openModal: boolean = false;
  icon: string = 'Attachment';
  iconStyle: string = 'icon-modal';

  isTableAttachment!: boolean;

  isTableAttachmentIndicePiao!: boolean;

  ngOnInit(): void {
    this.isTableAttachment =
      this.codTipologiaAllegato === CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE;

    this.isTableAttachmentIndicePiao = this.codTipologia === CodTipologiaSezioneEnum.PIAO;

    this.getAllAttachment();
  }

  getAllAttachment() {
    this.attachmentService
      .getAllAttachmentsByTipologia(this.codTipologia, this.codTipologiaAllegato, this.idPiao)
      .pipe(map((res) => res.data))
      .subscribe({
        next: (value: AllegatoDTO[]) => {
          value.forEach((x) => {
            if (this.isOnlyAttachment) {
              this.form.patchValue({ allegati: value });
            }
            let splitCodDocumento = x.codDocumento?.split('.') || [];
            x.codDocumentoFE = splitCodDocumento[0];
            x.type = splitCodDocumento[1]?.toUpperCase();
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
    this.attachmentService.deleteAttachment(idDoc, codDocumento).subscribe({
      next: (value: any) => {
        this.getAllAttachment();
      },
      error: (err: any) => {},
    });
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
      codTipologiaAllegato: this.codTipologiaAllegato,
      codTipologiaFK: this.codTipologia,
      idEntitaFK: this.idPiao,
    };

    this.attachmentService
      .saveAttachment(allegatoDTO, this.child.formGroup.controls['file'].value)
      .subscribe({
        next: (value: any) => {
          this.getAllAttachment();
          this.openModal = false;
        },
        error: (err: any) => {
          console.log('value', err);
        },
      });
  }
}
