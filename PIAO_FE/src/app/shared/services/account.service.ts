import { Injectable } from '@angular/core';
import { HttpClient, HttpContext, HttpHeaders } from '@angular/common/http';
import { map, Observable, of, tap } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { BYPASS_APP_INTERCEPTORS } from '../interceptors/interceptor.tokens';
import { FunzionalitaDTO } from '../models/classes/funzionalita-dto';
import { UtenteDTO } from '../models/classes/utente-dto';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  public user: UtenteDTO | null = null;  // Variabile per salvare l'utente in memory
  public funzionalitaList: string[] | null = null;  // Variabile per salvare l'utente in memory

  constructor(private http: HttpClient) { }


  getAccount(): Observable<UtenteDTO | null> {
    if (this.user != null) {
      return of(this.user); // già in memoria
    }

    return this.fetchUser().pipe( // non in memoria effettuo la fetch
      map(res => res.data),
      tap(user => {
        this.user = user;
      })
    );
  }

  getFunzionalita(role: string[]): Observable<string[] | null> {
    if (this.funzionalitaList != null) {
      return of(this.funzionalitaList); // già in memoria
    }

    return this.fetchFunzionalita(role).pipe( // non in memoria effettuo la fetch
      map(res => {
        const funzionalitaRes = res.data ?? [];
        return funzionalitaRes.map(f => f.codiceFunzionalita);
      }),
      tap(funzionalita => {
        this.funzionalitaList = funzionalita;
      })
    );

  }

  private fetchUser(): Observable<GenericResponse<UtenteDTO | null>> {
    //bypass per l'errorInterceptor
    const context = new HttpContext().set(BYPASS_APP_INTERCEPTORS, true);

    return this.http.get<GenericResponse<UtenteDTO>>(
      Path.url('/tokenized/user'),
      {
        context: context
      }
    );
  }

  private fetchFunzionalita(role: string[]): Observable<GenericResponse<FunzionalitaDTO[] | null>> {
    return this.http.post<GenericResponse<FunzionalitaDTO[]>>(
      Path.url('/funzionalita/by-ruolo'),
      role
    );

  }
}
