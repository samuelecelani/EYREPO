import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PIAODTO } from '../models/classes/piao-dto';
import { GenericResponse } from '../models/interfaces/generic-response';
import { Path } from '../utils/path';
import { map, Observable, tap } from 'rxjs';
import { StrutturaIndicePiaoDTO } from '../models/classes/struttura-indice-piao-dto';
import { TabellaValidazioneDTO } from '../models/classes/tabella-validazione-dto';
import { TabellaStoricoSezioneDTO } from '../models/classes/tabella-storico-sezione-dto';
import { AutoritaApprovatoreDTO } from '../models/classes/autorita-approvatore-dto';

@Injectable({
  providedIn: 'root',
})
export class PIAOService {
  constructor(private http: HttpClient) {}

  getOrCreatePiao(piaoDTO: PIAODTO, triennioRiferimento: string): Observable<PIAODTO> {
    let params = new HttpParams().set('triennioRiferimento', triennioRiferimento);
    return this.http
      .post<GenericResponse<PIAODTO>>(Path.url('/piao/initialize'), piaoDTO, { params })
      .pipe(map((res) => res.data));
  }

  redigiPiaoIsAllowed(codPA: string): Observable<boolean> {
    const params = new HttpParams().set('codPAFK', codPA);

    return this.http
      .get<GenericResponse<boolean>>(Path.url('/piao/redigi/allowed'), {
        params,
      })
      .pipe(map((res) => res.data));
  }

  getPiaoPrecedente(codPA: string): Observable<PIAODTO | null> {
    const params = new HttpParams().set('codPAFK', codPA);
    return this.http
      .get<GenericResponse<PIAODTO>>(Path.url('/piao/precedente'), {
        params,
      })
      .pipe(map((res) => (res ? res.data || null : null)));
  }

  getTipologiaCorrente(codPA: string): Observable<PIAODTO | null> {
    const params = new HttpParams().set('codPAFK', codPA);
    return this.http
      .get<GenericResponse<PIAODTO>>(Path.url('/piao/tipologia-corrente'), {
        params,
      })
      .pipe(map((res) => (res ? res.data || null : null)));
  }

  getStructureIndicePIAO(idPiao?: number): Observable<StrutturaIndicePiaoDTO[]> {
    const params = idPiao ? new HttpParams().set('idPiao', idPiao) : {};
    return this.http
      .get<GenericResponse<StrutturaIndicePiaoDTO[]>>(Path.url('/struttura/piao'), {
        params,
      })
      .pipe(
        map((res) => res.data),
        tap((res) => {
          res.forEach((x) => {
            x.children.forEach((y) => {
              y.numeroSezione = y.numeroSezione.split('').join('.');
            });
          });
        })
      );
  }

  getAllPiao(codPA: string): Observable<PIAODTO[]> {
    const params = new HttpParams().set('codPAFK', codPA);

    return this.http
      .get<GenericResponse<PIAODTO[]>>(Path.url('/piao/findAllPiao'), {
        params,
      })
      .pipe(map((res) => res.data));
  }

  getStructureValidazionePiao(idPiao: number): Observable<TabellaValidazioneDTO[]> {
    const params = new HttpParams().set('idPiao', idPiao);
    return this.http
      .get<GenericResponse<TabellaValidazioneDTO[]>>(Path.url('/struttura/validazione'), {
        params,
      })
      .pipe(map((res) => res.data));
  }

  accettaValidazioneSezioniSelezionate(
    idPiao: number,
    idSezione: Map<string, number>
  ): Observable<void> {
    const params = new HttpParams().set('idPiao', idPiao.toString());

    // converto Map -> oggetto JSON
    const body: { [key: string]: number } = Object.fromEntries(idSezione);

    return this.http.patch<void>(Path.url('/struttura/validazione/accetta-selezionate'), body, {
      params,
    });
  }

  validaSezione(
    idSezione: number,
    sezione: string,
    testoSezione: string,
    campiModificati: string
  ): Observable<void> {
    let params = new HttpParams()
      .set('testoSezione', testoSezione)
      .set('campiModificati', campiModificati);
    const codicePa = this.getCodicePaFromSession();
    if (codicePa) {
      params = params.set('codicePa', codicePa);
    }
    return this.http.patch<void>(
      Path.url(`/${sezione}/valida-sezione/${idSezione}`),
      {},
      { params }
    );
  }

  rifiutaSezione(
    idSezione: number,
    sezione: string,
    body: any,
    testoSezione: string,
    campiModificati: string
  ): Observable<void> {
    let params = new HttpParams()
      .set('testoSezione', testoSezione)
      .set('campiModificati', campiModificati);
    const codicePa = this.getCodicePaFromSession();
    if (codicePa) {
      params = params.set('codicePa', codicePa);
    }
    return this.http.patch<void>(Path.url(`/${sezione}/rifiuta-validazione/${idSezione}`), body, {
      params,
    });
  }

  revocaSezione(
    idSezione: number,
    sezione: string,
    body: any,
    testoSezione: string,
    campiModificati: string
  ): Observable<void> {
    let params = new HttpParams()
      .set('testoSezione', testoSezione)
      .set('campiModificati', campiModificati);
    const codicePa = this.getCodicePaFromSession();
    if (codicePa) {
      params = params.set('codicePa', codicePa);
    }
    return this.http.patch<void>(Path.url(`/${sezione}/revoca-validazione/${idSezione}`), body, {
      params,
    });
  }

  annullaValidazioneSezione(
    idSezione: number,
    sezione: string,
    testoSezione: string,
    campiModificati: string
  ): Observable<void> {
    let params = new HttpParams()
      .set('testoSezione', testoSezione)
      .set('campiModificati', campiModificati);
    const codicePa = this.getCodicePaFromSession();
    if (codicePa) {
      params = params.set('codicePa', codicePa);
    }
    return this.http.patch<void>(
      Path.url(`/${sezione}/annulla-validazione/${idSezione}`),
      {},
      { params }
    );
  }

  getStoricoModificheSezione(idSezione: number, codTipologiaFK: string) {
    const params = new HttpParams()
      .set('idSezione', idSezione)
      .set('codTipologiaFK', codTipologiaFK);

    return this.http
      .get<GenericResponse<TabellaStoricoSezioneDTO[]>>(Path.url('/storico-modifica'), { params })
      .pipe(map((res) => res.data));
  }

  getPiaoPubblicati(
    codPAFK: string,
    denominazione: string,
    versione?: string
  ): Observable<PIAODTO[]> {
    let params = new HttpParams().set('codPAFK', codPAFK).set('denominazione', denominazione);
    if (versione) {
      params = params.set('versione', versione);
    }

    return this.http
      .get<GenericResponse<PIAODTO[]>>(Path.url('/piao/findByDenominazioneVersione'), { params })
      .pipe(map((res) => res.data));
  }

  getTrienniRiferimento(): Observable<string[]> {
    return this.http
      .get<GenericResponse<string[]>>(Path.url('/piao/trienni-riferimento'))
      .pipe(map((res) => res.data ?? []));
  }

  getAutoritaApprovatore(): Observable<AutoritaApprovatoreDTO[]> {
    return this.http
      .get<GenericResponse<AutoritaApprovatoreDTO[]>>(Path.url('/autorita-approvatore'))
      .pipe(map((res) => res.data ?? []));
  }

  consultazionePiao(
    codPaFK: string,
    denominazione: string,
    versione?: string
  ): Observable<PIAODTO[]> {
    const params = new HttpParams().set('codPAFK', codPaFK).set('denominazione', denominazione);
    if (versione) {
      params.set('versione', versione);
    }
    return this.http
      .get<GenericResponse<PIAODTO[]>>(Path.url('/piao/findByDenominazioneVersione'), { params })
      .pipe(map((res) => res.data));
  }

  getPiaoPubblicatiSearchDFP(codiceIpa?: string, tipologia?: string): Observable<PIAODTO[]> {
    let params = new HttpParams();
    if (codiceIpa) {
      params = params.set('codiceIpa', codiceIpa);
    }
    if (tipologia) {
      params = params.set('tipologia', tipologia);
    }
    return this.http
      .get<GenericResponse<PIAODTO[]>>(Path.url('/piao/pubblicati/search'), { params })
      .pipe(map((res) => res.data));
  }

  getPiaoPubblicatiSearchDFPMassivo(
    tipologia?: string,
    denominazione?: string
  ): Observable<PIAODTO[]> {
    let params = new HttpParams();
    if (denominazione) {
      params = params.set('denominazione', denominazione);
    }
    if (tipologia) {
      params = params.set('tipologia', tipologia);
    }
    return this.http
      .get<
        GenericResponse<PIAODTO[]>
      >(Path.url('/piao/pubblicati/search-by-denominazione'), { params })
      .pipe(map((res) => res.data));
  }

  private getCodicePaFromSession(): string | null {
    try {
      const paAttivaRaw = sessionStorage.getItem('paAttivaDTO');
      if (!paAttivaRaw) return null;
      const paAttiva = JSON.parse(paAttivaRaw);
      return typeof paAttiva?.codePA === 'string' && paAttiva.codePA ? paAttiva.codePA : null;
    } catch {
      return null;
    }
  }

  saveBozzaPiaoPDF(piao: PIAODTO): Observable<GenericResponse<void>> {
    return this.http.put<GenericResponse<void>>(Path.url('/piao/salva-bozza-pdf'), piao);
  }
  pubblicaPiaoPDF(piao: PIAODTO): Observable<GenericResponse<void>> {
    return this.http.put<GenericResponse<void>>(Path.url('/piao/pubblica-pdf'), piao);
  }
}
