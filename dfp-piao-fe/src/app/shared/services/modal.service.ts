import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { TypeErrorEnum } from '../models/enums/type-error.enum';
import { MetadatoDTO } from '../models/interfaces/metadato-dto';

export interface ModalData {
  title: string;
  message: string;
  typeEnum: TypeErrorEnum;
  metadato: MetadatoDTO<any[]>[];
}

export interface ModalConfirmAction {
  metadato: MetadatoDTO<any>;
}

@Injectable({ providedIn: 'root' })
export class ModalService {
  private openModal$ = new Subject<ModalData>();
  onOpenModal$ = this.openModal$.asObservable();

  private confirmAction$ = new Subject<ModalConfirmAction>();
  onConfirmAction$ = this.confirmAction$.asObservable();

  open(
    title: string,
    message: string,
    typeEnum: TypeErrorEnum,
    metadato: MetadatoDTO<any>[]
  ): void {
    this.openModal$.next({ title, message, typeEnum, metadato });
  }

  emitConfirmAction(metadato: MetadatoDTO<any>): void {
    this.confirmAction$.next({ metadato });
  }
}
