import { EventiNotificheEnum } from '../enums/eventi-notifiche.enum';

export interface INotification {
  // Schema API aggiornato
  id: number;
  message: string;
  sender: string;
  ready: boolean;
  read: boolean;
  creationDate: string;
  readDate?: string;
  type: 'EMAIL' | 'SMS' | 'PUSH' | string;
  idModulo: string;

  // Campi legacy per retrocompatibilità (opzionali)
  idNotifica?: string;
  codiceEvento?: EventiNotificheEnum;
  dataOraUTC?: Date;
  titolo?: string;
  body?: string;
  destinatario?: string;
  isRead?: boolean;
}
