import { Path } from '../utils/path';

export interface IPIAOConfig {
  apiEndpoint: string;
  isProduction: boolean;

  // Auth / OpenID
  loginType: string;
  localStorageToken: string;
  tokenHeader: string;
  tokenHeaderPrefix: string;
  openidIssuerUrl: string;
  openidRedirectUri: string;
  openidLogoutUrl: string;
  openidPostlogoutRedirectUri: string;
  openidClientId: string;
  openidResponseType: string;
  openidScope: string;
  openidOidc: boolean;
  openidShowDebugInfo: boolean;
  openidRequireHttps: boolean;
  openidSkipIssuerCheck: boolean;
  openidStrictDiscoveryDocValidation: boolean;
  openidPreserveRequestRoute: boolean;
  openidTimeoutFactor: number;

  // PDF quality thresholds
  pdfLowTextThreshold: number;
  pdfMinTotalChars: number;
  pdfMinDensityPerKb: number;
  pdfMaxLowPagesPct: number;

  // Altri parametri di configurazione...
  dataCompilazionePiao: string;
  dataScadenzaPiao: string;

  //URL Minerva
  publicUrlMinerva: string;

  //URL Portale Performance
  publicUrlPortalePerformance: string;
}

let loadedConfig!: IPIAOConfig;

export function loadPIAOConfig(): Promise<void> {
  const isLocal = window.location.hostname === 'localhost';
  // In locale usiamo il proxy Angular (same-origin) per supportare i cookie
  // In produzione usiamo window.location.origin
  const apiBase = isLocal ? '' : window.location.origin;
  const url = new URL(
    '/area-riservata/config/initializer',
    apiBase || window.location.origin
  ).toString();

  return fetch(url, {
    method: 'GET',
    credentials: 'include',
    headers: { 'id-spinner': 'none' },
  })
    .then((res) => {
      if (!res.ok) throw new Error(`Config HTTP ${res.status}`);
      return res.json();
    })
    .then((json) => {
      if (!json || typeof json !== 'object' || !json.data || typeof json.data !== 'object')
        throw new Error('Config BE not valid');
      loadedConfig = json.data;
      // In sviluppo locale, estraiamo solo il path da /api in poi per usare il proxy Angular
      if (isLocal) {
        const idx = loadedConfig.apiEndpoint.indexOf('/api');
        const apiEndpoint = idx !== -1 ? loadedConfig.apiEndpoint.substring(idx) : '/api';
        loadedConfig = { ...loadedConfig, apiEndpoint };
      }
      Path.setup(loadedConfig);
    });
}

export function getConfig(): IPIAOConfig {
  return loadedConfig;
}

export function getValue<T extends keyof IPIAOConfig>(key: T): IPIAOConfig[T] {
  return loadedConfig[key];
}
