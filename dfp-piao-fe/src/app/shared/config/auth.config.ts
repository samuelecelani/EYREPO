import { AuthConfig } from 'angular-oauth2-oidc';
import { IPIAOConfig } from './loader-config';

export function buildAuthConfig(config: IPIAOConfig): AuthConfig {
  return {
    issuer: config.openidIssuerUrl,
    redirectUri: config.openidRedirectUri,
    postLogoutRedirectUri: config.openidPostlogoutRedirectUri,
    logoutUrl: config.openidLogoutUrl,
    revocationEndpoint: config.openidLogoutUrl,
    clientId: config.openidClientId,
    responseType: config.openidResponseType,
    scope: config.openidScope,
    showDebugInformation: config.openidShowDebugInfo,
    requireHttps: config.openidRequireHttps,
    skipIssuerCheck: config.openidSkipIssuerCheck,
    strictDiscoveryDocumentValidation: config.openidStrictDiscoveryDocValidation,
    preserveRequestedRoute: config.openidPreserveRequestRoute,
    timeoutFactor: config.openidTimeoutFactor,
    oidc: config.openidOidc,
  };
}
