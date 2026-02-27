import { environment } from '../../../environments/environment';
import { Path } from '../utils/path';

export interface IPIAOConfig {
  apiEndpoint: string;
  production: boolean;
}

let loadedConfig!: IPIAOConfig;

export function loadPIAOConfig(): Promise<void> {
  const isLocal = window.location.hostname === 'localhost';
  const apiBase = isLocal ? environment.apiUrl : window.location.origin;
  const url = new URL('api/v1/config/initializer', apiBase).toString();

  return fetch(url, { method: 'GET' })
    .then((res) => {
      if (!res.ok) throw new Error(`Config HTTP ${res.status}`);
      return res.json();
    })
    .then((json) => {
      if (!json || typeof json !== 'object' || !json.data || typeof json.data !== 'object')
        throw new Error('Config BE not valid');
      loadedConfig = json.data;
      Path.setup(loadedConfig);
    });
}
