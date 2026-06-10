import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth.guard';
import { piaoSessionStorageGuard } from './shared/guards/piao-session-storage.guard';
import { roleRouteGuard } from './shared/guards/role-route.guard';
import { roleHomeRedirectGuard } from './shared/guards/role-home-redirect.guard';
import { BaseLayoutComponent } from './shared/components/base-layout/base-layout.component';

/**
 * LAZY LOADING:
 * - loadComponent: carica il componente solo quando serve (code splitting)
 * - Migliora performance iniziali dell'app
 */
export const routes: Routes = [
  // Route home (/) - redirect a login
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'auth/login',
  },
  {
    path: 'pubblicato',
    loadComponent: () =>
      import('./pages/piao-pubblicato/piao-pubblicato.component').then(
        (m) => m.PiaoPubblicatoComponent
      ),
  },
  /*
  {
    path: 'index',
    loadComponent: () => import('./pages/index/index.component').then((m) => m.IndexComponent),
    canActivate: [authGuard],
  },
  */

  /*
    Pagina Login con header e footer ma senza sidebar
  */
  {
    path: '',
    component: BaseLayoutComponent,
    children: [
      {
        path: 'auth/login',
        loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
      },
    ],
  },
  // Tutte le altre route - CON layout (header/footer/sidebar + outlet dinamico)
  {
    path: '',
    component: BaseLayoutComponent,
    canActivate: [authGuard],
    canActivateChild: [roleRouteGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [roleHomeRedirectGuard],
        children: [],
      },
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
        path: 'validazione',
        loadComponent: () =>
          import('./pages/scrivania-pa/validazione/validazione.component').then(
            (m) => m.ValidazioneComponent
          ),
      },
      {
        path: 'area-privata-PA',
        loadComponent: () =>
          import('./pages/scrivania-pa/scrivania-pa.component').then((m) => m.ScrivaniaPAComponent),
      },
      {
        path: 'area-privata-DFP',
        loadComponent: () =>
          import('./pages/scrivania-dfp/scrivania-dfp.component').then(
            (m) => m.ScrivaniaDfpComponent
          ),
      },
      {
        path: 'cruscotti-di-analisi',
        data: {
          title: 'Cruscotti di analisi',
          description: 'Landing page provvisoria per la sezione Cruscotti di analisi (DFP).',
        },
        loadComponent: () =>
          import('./pages/dfp-placeholder/dfp-placeholder.component').then(
            (m) => m.DfpPlaceholderComponent
          ),
      },
      {
        path: 'gestione-piao',
        loadComponent: () =>
          import('./pages/scrivania-dfp/gestione-piao/gestione-piao.component').then(
            (m) => m.GestionePiaoComponent
          ),
      },
      {
        path: 'gestione-piao/revisione',
        loadComponent: () =>
          import('./pages/scrivania-dfp/gestione-piao/revisione-piao/revisione-piao.component').then(
            (m) => m.RevisionePiaoComponent
          ),
      },
      {
        path: 'gestione-piao/scarico-massivo',
        loadComponent: () =>
          import('./pages/scrivania-dfp/gestione-piao/scarico-massivo-piao/scarico-massivo-piao.component').then(
            (m) => m.ScaricoMassivoPiaoComponent
          ),
      },
      {
        path: 'gestione-piao/revisione/piao-pdf',
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/carica-piao/carica-piao.component').then(
            (m) => m.CaricaPiaoComponent
          ),
      },
      {
        path: 'gestione-piao/revisione/indice-piao',
        canActivate: [piaoSessionStorageGuard],
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/indice-piao/indice-piao.component').then(
            (m) => m.IndicePiaoComponent
          ),
      },
      {
        path: 'gestione-piao/revisione/indice-piao/sezione',
        canActivate: [piaoSessionStorageGuard],
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/indice-piao/sezione/sezione.component').then(
            (m) => m.SezioneComponent
          ),
      },
      {
        path: 'storico-dichiarazioni',
        data: {
          title: 'Storico dichiarazioni',
        },
        loadComponent: () =>
          import('./pages/scrivania-dfp/storico-dichiarazioni/storico-dichiarazioni.component').then(
            (m) => m.StoricoDichiarazioniComponent
          ),
      },
      {
        path: 'solleciti',
        data: {
          title: 'Invia sollecito alle PA per il PIAO 25-27',
        },
        loadComponent: () =>
          import('./pages/scrivania-dfp/solleciti/solleciti.component').then(
            (m) => m.SollecitiComponent
          ),
      },
      {
        path: 'solleciti/dettaglio-mancata-compilazione/:id',
        data: {
          title: 'Dettaglio dichiarazione di mancata o ritardata compilazione',
          isDettaglioStorico: true,
        },
        loadComponent: () =>
          import('./pages/scrivania-pa/mancata-compilazione/mancata-compilazione.component').then(
            (m) => m.MancataCompilazioneComponent
          ),
      },
      {
        path: 'storico-dichiarazioni/dettaglio-mancata-compilazione/:id',
        data: {
          title: 'Dettaglio dichiarazione di mancata o ritardata compilazione',
          isDettaglioStorico: true,
        },
        loadComponent: () =>
          import('./pages/scrivania-pa/mancata-compilazione/mancata-compilazione.component').then(
            (m) => m.MancataCompilazioneComponent
          ),
      },
      {
        path: 'avvisi/pubblica-avviso',
        data: {
          title: 'Pubblica un avviso o una comunicazione',
        },
        loadComponent: () =>
          import('./pages/scrivania-dfp/avvisi/avviso-form/avviso-form.component').then(
            (m) => m.AvvisoFormComponent
          ),
      },
      {
        path: 'avvisi/dettaglio-avviso/:id',
        data: {
          title: 'Dettaglio avviso o comunicazione',
          isDetails: true,
        },
        loadComponent: () =>
          import('./pages/scrivania-dfp/avvisi/avviso-form/avviso-form.component').then(
            (m) => m.AvvisoFormComponent
          ),
      },
      {
        path: 'avvisi/modifica-avviso/:id',
        data: {
          title: 'Modifica avviso o comunicazione',
        },
        loadComponent: () =>
          import('./pages/scrivania-dfp/avvisi/avviso-form/avviso-form.component').then(
            (m) => m.AvvisoFormComponent
          ),
      },
      {
        path: 'avvisi',
        data: {
          title: 'Avvisi',
          description: 'Gestione avvisi e comunicazioni.',
        },
        loadComponent: () =>
          import('./pages/scrivania-dfp/avvisi/avvisi.component').then((m) => m.AvvisiComponent),
      },
      {
        path: 'area-privata-PA/mancata-compilazione',
        loadComponent: () =>
          import('./pages/scrivania-pa/mancata-compilazione/mancata-compilazione.component').then(
            (m) => m.MancataCompilazioneComponent
          ),
      },
      {
        path: 'novita/dettaglio/:id',
        loadComponent: () =>
          import('./pages/scrivania-pa/novita-detail/novita-detail.component').then(
            (m) => m.NovitaDetailComponent
          ),
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
        path: 'gestionale/gestione-profilo-utente/new',
        loadComponent: () =>
          import('./pages/scrivania-pa/gestionale/gestione-profilo-utente/gestione-profilo-utente.component').then(
            (m) => m.GestioneProfiloUtenteComponent
          ),
      },
      {
        path: 'gestionale/gestione-profilo-utente/:id',
        loadComponent: () =>
          import('./pages/scrivania-pa/gestionale/gestione-profilo-utente/gestione-profilo-utente.component').then(
            (m) => m.GestioneProfiloUtenteComponent
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
        canActivate: [piaoSessionStorageGuard],
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/indice-piao/indice-piao.component').then(
            (m) => m.IndicePiaoComponent
          ),
      },
      {
        path: 'servizi-piao/indice-piao/sezione',
        canActivate: [piaoSessionStorageGuard],
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/indice-piao/sezione/sezione.component').then(
            (m) => m.SezioneComponent
          ),
      },
      {
        path: 'servizi-piao/piao-pdf',
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/carica-piao/carica-piao.component').then(
            (m) => m.CaricaPiaoComponent
          ),
      },
      {
        path: 'notifiche',
        loadComponent: () =>
          import('./pages/notifiche/notifiche.component').then((m) => m.NotificheComponent),
      },
      {
        path: 'servizi-piao/consulta-piao',
        loadComponent: () =>
          import('./pages/scrivania-pa/servizi-piao/consultazione.component').then(
            (m) => m.ConsultazioneComponent
          ),
      },
    ],
  },
  // Wildcard route - redirect alla home se la rotta non esiste
  { path: '**', redirectTo: '' },
];
