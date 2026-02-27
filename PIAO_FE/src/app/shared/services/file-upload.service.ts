import { inject, Injectable } from '@angular/core';
import { Observable, catchError, map } from 'rxjs';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Path } from '../utils/path';

/**
 * DTO per i parametri richiesti dall'upload
 */
export interface UploadFonteDatiParams {
  dataFonteDati: string; // formato: YYYY-MM-DD
  tipoFonteDati: number;
}

/**
 * FileUploadService - Servizio per gestire l'upload di file
 *
 * ANALOGIA REACT:
 * ```typescript
 * const uploadFile = async (file: File, params: UploadParams) => {
 *   const formData = new FormData();
 *   formData.append('file', file);
 *
 *   const response = await fetch('/fonte-dati/upload?...', {
 *     method: 'POST',
 *     body: formData
 *   });
 * };
 * ```
 *
 * COME FUNZIONA:
 * - Crea un FormData object con il file
 * - Aggiunge query params (data e tipo)
 * - Invia la richiesta multipart/form-data
 * - Non setta Content-Type (il browser lo fa automaticamente con boundary)
 */
@Injectable({ providedIn: 'root' })
export class FileUploadService {
  private http = inject(HttpClient);

  /**
   * Upload file a /fonte-dati/upload
   *
   * @param file - Il file da caricare
   * @param params - Parametri obbligatori (dataFonteDati, tipoFonteDati)
   * @returns Observable della risposta del server
   *
   * ESEMPIO USO:
   * ```typescript
   * this.fileUpload.uploadFonteDati(file, {
   *   dataFonteDati: '2025-01-15',
   *   tipoFonteDati: 1
   * }).subscribe({
   *   next: (response) => console.log('Upload ok:', response),
   *   error: (err) => console.error('Upload fallito:', err)
   * });
   * ```
   */
  uploadFonteDati(file: File, params: UploadFonteDatiParams): Observable<any> {
    // Crea FormData per multipart/form-data
    const formData = new FormData();
    formData.append('file', file, file.name);

    // Crea query params
    const queryParams = new HttpParams()
      .set('dataFonteDati', params.dataFonteDati)
      .set('tipoFonteDati', params.tipoFonteDati.toString());

    // Risolve URL completo
    const baseUrl = Path.baseUrl();
    const url = `${baseUrl}/fonte-dati/upload`;

    // POST con FormData (il browser aggiunge automaticamente Content-Type: multipart/form-data)
    return this.http
      .post<any>(url, formData, {
        params: queryParams,
        // Non settiamo Content-Type: il browser lo fa automaticamente con boundary
        withCredentials: true,
        observe: 'response', // Osserva la risposta completa per accedere allo status
      })
      .pipe(
        map((response: any) => {
          // Estrae il body (o {} se vuoto)
          return response.body ?? {};
        }),
        catchError((error: unknown) => {
          // Se l'errore ha status 2xx (es. errore di parsing), è comunque un successo
          if (error instanceof HttpErrorResponse && error.status >= 200 && error.status < 300) {
            // Status 2xx = successo, anche se il body non è parsabile
            console.log('✅ Upload completato con status 200 (corpo vuoto o non-JSON)');
            return [{}];
          }
          // Status 4xx, 5xx o errori di rete = errore reale
          throw error;
        })
      );
  }
}
