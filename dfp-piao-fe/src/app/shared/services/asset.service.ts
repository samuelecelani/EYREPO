import { Injectable } from '@angular/core';
import { isLocalhost } from '../utils/utils';

/**
 * Risolve i path degli asset statici tenendo conto del prefisso
 * di deploy ('/area-riservata') quando l'app non gira in locale.
 *
 * Uso tipico nel template:
 *   <div [style.background-image]="asset.bg('img/header.png')"></div>
 *   <img [src]="asset.url('icon/foo.svg')" />
 */
@Injectable({ providedIn: 'root' })
export class AssetService {
  private readonly prefix = isLocalhost() ? '' : '/area-riservata';

  /** Restituisce l'URL assoluto dell'asset (es. "/area-riservata/assets/img/x.png"). */
  url(path: string): string {
    const normalized = path.startsWith('/') ? path.slice(1) : path;
    return `${this.prefix}/assets/${normalized}`;
  }

  /** Restituisce un valore pronto da usare in `background-image`. */
  bg(path: string): string {
    return `url('${this.url(path)}')`;
  }
}
