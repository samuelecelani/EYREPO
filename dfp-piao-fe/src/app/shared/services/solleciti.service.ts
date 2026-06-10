import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { GenericResponse } from "../models/interfaces/generic-response";
import { Path } from "../utils/path";
import { map } from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class SollecitiService 
{
    constructor(private http: HttpClient) {}

    getValoreFromCodice(codice: string) 
    {
        const params = new HttpParams()
        .set('codice', codice)
        return this.http
          .get<
            GenericResponse<string>
          >(Path.url(`/configurazioni/value`), { params })
          .pipe(map((response) => response?.data ?? ''));
    }

    setValoreFromCodice(codice: string, valore: string) 
    {
        return this.http
          .put<
            GenericResponse<string>
          >(Path.url(`/configurazioni`), { codice, valore })
          .pipe(map((response) => response?.data ?? ''));
    }
}