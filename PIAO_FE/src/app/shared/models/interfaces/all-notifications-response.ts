import { INotification } from './notification';

export interface IAllNotificationsResponse {
  risultati: INotification[];
  totalePagine: number;
  totaleElementi: number;
}
