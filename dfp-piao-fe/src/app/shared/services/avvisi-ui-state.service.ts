import { Injectable } from '@angular/core';

export interface IAvvisoAttachment {
  id: string;
  name: string;
  type: string;
  sizeLabel: string;
  objectUrl: string;
}

@Injectable({ providedIn: 'root' })
export class AvvisiUiStateService {
  private attachmentsByAvviso = new Map<string, IAvvisoAttachment[]>();

  getAttachments(avvisoId: string): IAvvisoAttachment[] {
    return [...(this.attachmentsByAvviso.get(avvisoId) || [])];
  }

  setAttachments(avvisoId: string, attachments: IAvvisoAttachment[]): void {
    this.attachmentsByAvviso.set(avvisoId, [...attachments]);
  }

  clearAttachments(avvisoId: string): void {
    this.attachmentsByAvviso.delete(avvisoId);
  }
}