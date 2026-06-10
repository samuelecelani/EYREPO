export interface INotificationRead {
  id: number;
  message: string;
  sender: string;
  ready: boolean;
  read: boolean;
  creationDate: string;
  readDate?: string | null;
  type: string;
  idModulo: string;
}
