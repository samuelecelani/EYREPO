import { IPIAOConfig } from '../config/loader-config';

export class Path {
  private static _cfg: IPIAOConfig;

  static setup(cfg: IPIAOConfig) {
    this._cfg = cfg;
  }

  static url(subPath: string): string {
    return this.baseUrl() + subPath;
  }

  static baseUrl(): string {
    return this._cfg.apiEndpoint;
  }
}
