import { inject, Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { BaseApiService } from './base-api.service';

/**
 * Interfaccia per additionalInfo
 */
export interface AdditionalInfo {
  id?: string;
  externalId?: number;
  properties?: Array<{
    key: string;
    value: string;
  }>;
}

/**
 * Interfaccia per la creazione di un test (POST)
 */
export interface CreateTestDto {
  createdBy: string;
  updatedBy: string;
  testo: string;
  additionalInfo?: AdditionalInfo;
}

/**
 * Interfaccia per il risultato del test (GET)
 */
export interface TestDto {
  validity: boolean;
  createdBy: string;
  createdTs: string;
  updatedBy: string | null;
  updatedTs: string | null;
  id: number;
  testo: string;
  additionalInfo?: AdditionalInfo;
}

/**
 * Interfaccia per lo status
 */
interface ApiStatus {
  success: boolean;
}

/**
 * Interfaccia per l'errore
 */
interface ApiError {
  messageError?: string;
  errorCode?: string;
}

/**
 * Interfaccia per la risposta wrappata della GET
 */
interface TestsResponse {
  data: (TestDto | null)[];
  status?: ApiStatus;
  error?: ApiError;
}

@Injectable({ providedIn: 'root' })
export class UsersService {
  private api = inject(BaseApiService);

  /**
   * Recupera la lista dei test
   * GET /test
   * Restituisce { data: [...], status: {...}, error: {...} } e noi estraiamo solo l'array filtrato
   */
  list(): Observable<TestDto[]> {
    return this.api.get<TestsResponse>('/test').pipe(
      map((response) => {
        // Filtra i valori null dall'array data
        const tests = response?.data || [];
        return tests.filter((test: TestDto | null): test is TestDto => test !== null);
      })
    );
  }

  /**
   * Crea un nuovo test
   * POST /test/salva
   * Nota: il backend potrebbe non restituire un corpo completo, quindi usiamo any
   */
  create(body: CreateTestDto): Observable<any> {
    return this.api.post<any, CreateTestDto>('/test/salva', body);
  }
}
