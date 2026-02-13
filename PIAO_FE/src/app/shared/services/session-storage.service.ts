import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SessionStorageService {
  constructor() {}

  setItem(key: string, obj: any): void {
    sessionStorage.setItem(key, JSON.stringify(obj));
  }

  getItem(key: string): any | null {
    let obj = sessionStorage.getItem(key);
    if (obj) {
      return JSON.parse(obj);
    } else {
      return null;
    }
  }

  removeItem(key: string): void {
    sessionStorage.removeItem(key);
  }
}
