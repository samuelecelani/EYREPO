import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map } from 'rxjs';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { CodPathEnum } from '../models/enums/cod-path.enum';
import { CategoriaTicketDTO } from '../models/classes/categoria-ticket-dto';
import { TicketDTO } from '../models/classes/ticket-dto';
import { AllegatoTicketDTO } from '../models/classes/allegato-ticket-dto';

@Injectable({
  providedIn: 'root',
})
export class TicketService {
  constructor(private http: HttpClient) {}

  getCategorieByIdModulo(idModulo: string) {
    return this.http
      .get<
        GenericResponse<CategoriaTicketDTO[]>
      >(Path.url(`/${CodPathEnum.TICKET}/categorie/${idModulo}`))
      .pipe(map((res) => res.data));
  }

  createTicket(ticket: TicketDTO) {
    return this.http.post<GenericResponse<TicketDTO>>(Path.url(`/${CodPathEnum.TICKET}`), ticket);
  }

  getAllegatiByTicket(idTicket: number) {
    return this.http
      .get<
        GenericResponse<AllegatoTicketDTO[]>
      >(Path.url(`/${CodPathEnum.TICKET}/${idTicket}/allegati`))
      .pipe(map((res) => res.data));
  }

  saveAllegato(allegato: any, file: File) {
    const formData = new FormData();
    formData.append('allegato', new Blob([JSON.stringify(allegato)], { type: 'application/json' }));
    formData.append('file', file, allegato.codDocumento);
    return this.http
      .post<
        GenericResponse<AllegatoTicketDTO>
      >(Path.url(`/${CodPathEnum.TICKET}/allegato`), formData)
      .pipe(map((res) => res.data));
  }

  deleteAllegato(idAllegato: number) {
    return this.http
      .delete(Path.url(`/${CodPathEnum.TICKET}/allegato/${idAllegato}`))
      .pipe(map((res) => res));
  }
}
