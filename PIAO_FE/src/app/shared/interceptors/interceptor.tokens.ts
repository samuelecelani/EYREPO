import { HttpContextToken } from '@angular/common/http';

// Quando true, gli interceptor applicativi devono saltare l'elaborazione
export const BYPASS_APP_INTERCEPTORS = new HttpContextToken<boolean>(() => false);
