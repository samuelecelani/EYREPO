import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CampiModificatiService {
  private config: any;

  constructor(private http: HttpClient) {}

  async loadConfig() {
    this.config = await firstValueFrom(
      this.http.get('config-static-storico/storico-campi-config.json')
    );
  }
  //async loadConfig() {
  //   const url = 'config-static-storico/storico-campi-config.json';
  //   console.log('[CampiModificati] URL:', url);

  //   try {
  //     this.config = await firstValueFrom(this.http.get(url));
  //     console.log('[CampiModificati] Loaded config:', this.config);
  //   } catch (err) {
  //     console.error('[CampiModificati] ERROR loading config:', err);
  //   }
  // }

  getConfig() {
    return this.config;
  }
}
