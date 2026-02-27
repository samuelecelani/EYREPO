import {
  ApplicationConfig,
  provideZoneChangeDetection,
  importProvidersFrom,
  provideAppInitializer,
  inject,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { authInterceptor } from './shared/interceptors/auth.interceptor';
import { correlationIdInterceptor } from './shared/interceptors/correlation-id.interceptor';
import { errorInterceptor } from './shared/interceptors/error.interceptor';
import { APP_BASE_HREF, PlatformLocation } from '@angular/common';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { translateFactory } from './shared/config/translate-config';
import { TRANSLATE_HTTP_LOADER_CONFIG } from '@ngx-translate/http-loader';
import { spinnerInterceptor } from './shared/interceptors/spinner.interceptor';
import { loadPIAOConfig } from './shared/config/loader-config';
import { LoginService } from './shared/services/login.service';
import { loadCspNonce } from './shared/config/csp-nonce-config';
import { cfInterceptor } from './shared/interceptors/cf.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideAnimations(),
    provideHttpClient(
      withInterceptors([
        //authInterceptor,
        correlationIdInterceptor,
        errorInterceptor,
        spinnerInterceptor,
        cfInterceptor,
      ])
    ),
    importProvidersFrom(
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useFactory: translateFactory,
          deps: [HttpClient],
        },
        lang: 'it',
      })
    ),
    provideAppInitializer(() => {
      return loadPIAOConfig().catch((err) => {
        console.error('Errore caricamento config:', err);
      });
    }),

    provideAppInitializer(() => {
      return loadCspNonce().catch((err) => {
        console.error('Errore inizializzazione CSP nonce:', err);
      });
    }),

    {
      provide: APP_BASE_HREF,
      useFactory: (s: PlatformLocation) => s.getBaseHrefFromDOM(),
      deps: [PlatformLocation],
    },
    {
      provide: TRANSLATE_HTTP_LOADER_CONFIG,
      useValue: {
        prefix: './assets/i18n/',
        suffix: '.json',
      },
    },
  ],
};
