import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { map } from 'rxjs';
import { DichiarazioneScadenzaDTO } from '../models/classes/dichiarazione-scadenza-dto';
import { StoricoDichiarazioneDFPDTO } from '../models/classes/storico-dichiarazione-dfp-dto';
import {
  SollecitiDichiarazioniDFPDTO,
  StatoDichiarazione,
} from '../models/classes/solleciti-dichiarazioni-dfp-dto';
import { Page, Pageable, pageableToParams } from '../models/interfaces/pageable';

@Injectable({
  providedIn: 'root',
})
export class DichiarazioneScadenzaService {
  constructor(private http: HttpClient) {}

  getExistingDichiarazioneScadenza(codPAFK: string) {
    return this.http
      .get<
        GenericResponse<DichiarazioneScadenzaDTO>
      >(Path.url(`/dichiarazione-scadenza/${codPAFK}`))
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  /** Recupera la dichiarazione di scadenza per idPiao (usato dal flusso "dettaglio storico" DFP). */
  getDichiarazioneScadenzaByPiao(idPiao: number) {
    return this.http
      .get<
        GenericResponse<DichiarazioneScadenzaDTO>
      >(Path.url(`/dichiarazione-scadenza/by-piao/${idPiao}`))
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  save(dichiarazione: DichiarazioneScadenzaDTO) {
    return this.http
      .post<
        GenericResponse<DichiarazioneScadenzaDTO>
      >(Path.url('/dichiarazione-scadenza'), dichiarazione)
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  findAllaStoricoDichiarazioneScadenza() {
    return this.http
      .get<GenericResponse<StoricoDichiarazioneDFPDTO[]>>(Path.url('/dichiarazione-scadenza/all'))
      .pipe(map((res) => (res ? res.data || [] : [])));
  }

  updateStatoDichiarazioneScadenza(id: number, stato: boolean) {
    return this.http.patch<void>(Path.url(`/dichiarazione-scadenza/${id}/stato`), stato);
  }

  /**
   * Search NON paginato dei solleciti (uso semplice / dataset piccoli).
   */
  searchSolleciti(filters: {
    denominazionePiao: string;
    tipologiaIstat?: string | null;
    codPAFK?: string | null;
    statoDichiarazione?: StatoDichiarazione | null;
  }) {
    let params = new HttpParams().set('denominazionePiao', filters.denominazionePiao);
    if (filters.tipologiaIstat) params = params.set('tipologiaIstat', filters.tipologiaIstat);
    if (filters.codPAFK) params = params.set('codPAFK', filters.codPAFK);
    if (filters.statoDichiarazione)
      params = params.set('statoDichiarazione', filters.statoDichiarazione);

    return this.http
      .get<
        GenericResponse<SollecitiDichiarazioniDFPDTO[]>
      >(Path.url('/dichiarazione-scadenza/search'), { params })
      .pipe(map((res) => (res ? res.data || [] : [])));
  }

  /**
   * Search PAGINATO dei solleciti.
   * Ritorna direttamente la {@link Page} (sganciata dal GenericResponse) per consumo
   * immediato da parte del PaginationComponent in modalità server-side.
   */
  searchSollecitiPaged(
    filters: {
      denominazionePiao: string;
      tipologiaIstat?: string | null;
      codPAFK?: string | null;
      statoDichiarazione?: StatoDichiarazione | null;
    },
    pageable: Pageable
  ) {
    let params = new HttpParams().set('denominazionePiao', filters.denominazionePiao);
    if (filters.tipologiaIstat) params = params.set('tipologiaIstat', filters.tipologiaIstat);
    if (filters.codPAFK) params = params.set('codPAFK', filters.codPAFK);
    if (filters.statoDichiarazione)
      params = params.set('statoDichiarazione', filters.statoDichiarazione);

    // Append page/size/sort
    const pageParams = pageableToParams(pageable);
    Object.entries(pageParams).forEach(([k, v]) => {
      if (Array.isArray(v)) v.forEach((vv) => (params = params.append(k, vv)));
      else params = params.set(k, v);
    });

    return this.http
      .get<
        GenericResponse<Page<SollecitiDichiarazioniDFPDTO>>
      >(Path.url('/dichiarazione-scadenza/search/paged'), { params })
      .pipe(map((res) => (res ? res.data : undefined)));
  }

  sendSollecito(codiciPA: string[], keyTemplate: string) {
    let params = new HttpParams().set('key', keyTemplate);
    for (const cod of codiciPA) {
      params = params.append('codiciPa', cod);
    }
    return this.http.get(Path.url('/notification/send-email-referenti'), { params });
  }
}
