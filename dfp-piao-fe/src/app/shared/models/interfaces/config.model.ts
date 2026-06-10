export interface AppConfig {
  appName: string;
  apiBaseUrl: string;
  oauth: {
    clientId: string;
  };
  requestTimeoutMs: number;
  retry: {
    maxAttempts: number;
    baseDelayMs: number;
  };
}
