import { Inject, inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { WINDOW } from '../config/window-config';
import { Path } from '../utils/path';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  isLocal!: boolean;

  constructor(
    @Inject(WINDOW) public win: Window & typeof globalThis
  ) {
    this.isLocal = window.location.origin.includes('localhost:');
  }

  login(): void {
    const baseUrl = Path.baseUrl();
    this.win.location.assign(`${baseUrl}/auth/login?origin_uri=${this.isLocal ? window.location.href : `${baseUrl}/`}&redirect_uri=${baseUrl}/auth/callback`);
  }

  logout(): void {
    const baseUrl = Path.baseUrl();
    this.win.location.assign(`${baseUrl}/auth/logout?redirect_uri=${this.isLocal ? window.location.href : `${baseUrl}/`}`);
  }
}
