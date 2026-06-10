import { Inject, inject, Injectable } from '@angular/core';
import { WINDOW } from '../config/window-config';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  isLocal!: boolean;

  constructor(@Inject(WINDOW) public win: Window & typeof globalThis) {
    this.isLocal = window.location.origin.includes('localhost:');
  }

  login(): void {
    const baseUrl = Path.baseUrl();
    // In locale: origin_uri e redirect_uri devono puntare al FE (window.location.origin)
    // L'endpoint /api/v1/auth/login passa attraverso il proxy Angular
    const feOrigin = this.isLocal ? window.location.origin : baseUrl;
    const authEndpoint = baseUrl; // '' = proxy locale
    this.win.location.assign(
      `${authEndpoint}/auth/login?origin_uri=${encodeURIComponent(window.location.href)}&redirect_uri=${encodeURIComponent(feOrigin + `${authEndpoint}/auth/callback`)}`
    );
  }

  logout(): void {
    const baseUrl = Path.baseUrl();
    const feOrigin = this.isLocal ? window.location.origin : baseUrl;
    const authEndpoint = baseUrl;

    this.win.location.assign(
      `${authEndpoint}/auth/logout?redirect_uri=${encodeURIComponent(feOrigin + '/')}`
    );
  }
}
