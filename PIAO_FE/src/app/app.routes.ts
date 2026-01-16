import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth.guard';
import { BaseLayoutComponent } from './shared/components/base-layout/base-layout.component';

/**
 * LAZY LOADING:
 * - loadComponent: carica il componente solo quando serve (code splitting)
 * - Migliora performance iniziali dell'app
 */
export const routes: Routes = [
  // Route home (/) - SENZA layout (no header/footer/sidebar)
  {
    path: '',
    loadComponent: () => import('./pages/index/index.component').then((m) => m.IndexComponent),
    //canActivate: [authGuard],
  },
  // Tutte le altre route - CON layout (header/footer/sidebar + outlet dinamico)
  {
    path: 'pages',
    component: BaseLayoutComponent,
    //canActivate: [authGuard],
    children: [
      {
        path: 'profilo',
        loadComponent: () =>
          import('./pages/scrivania-pa/profilo/profilo.component').then((m) => m.ProfiloComponent),
      },
      {
        path: 'servizi-piao',
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/servizi-piao.component').then(
            (m) => m.ServiziPiaoComponent
          ),
      },
      {
        path: 'area-privata-PA',
        loadComponent: () =>
          import('./pages/scrivania-pa/scrivania-pa.component').then((m) => m.ScrivaniaPAComponent),
      },
      {
        path: 'novita',
        loadComponent: () =>
          import('./pages/scrivania-pa/novita-page/novita-page.component').then(
            (m) => m.NovitaPageComponent
          ),
      },
      {
        path: 'gestionale',
        loadComponent: () =>
          import('./pages/scrivania-pa/gestionale/gestionale.component').then(
            (m) => m.GestionaleComponent
          ),
      },
      {
        path: 'gestionale/dettaglio-attivita/:id',
        loadComponent: () =>
          import('./pages/scrivania-pa/gestionale/dettaglio-attivita/dettaglio-attivita.component').then(
            (m) => m.DettaglioAttivitaComponent
          ),
      },
      {
        path: 'help-desk',
        loadComponent: () =>
          import('./pages/scrivania-pa/help-desk/help-desk.component').then(
            (m) => m.HelpDeskComponent
          ),
      },
      {
        path: 'servizi-piao/indice-piao',
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/indice-piao/indice-piao.component').then(
            (m) => m.IndicePiaoComponent
          ),
      },
      {
        path: 'servizi-piao/indice-piao/sezione',
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/indice-piao/sezione/sezione.component').then(
            (m) => m.SezioneComponent
          ),
      },
      {
        path: 'notifiche',
        loadComponent: () =>
          import('./pages/notifiche/notifiche.component').then((m) => m.NotificheComponent),
      },
    ],
  },
  // Wildcard route - redirect alla home se la rotta non esiste
  { path: '**', redirectTo: '' },
];
