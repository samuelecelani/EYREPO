import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { IconName, getIcon } from '../../../../assets/svg-icons.registry';

/**
 * Button Component - Componente bottone riutilizzabile basato su Bootstrap Italia (italia.css)
 *
 * Utilizza le classi Bootstrap 5.2.3 per garantire coerenza con il design system Italia.
 * Il componente supporta diverse varianti, dimensioni, icone e stati.
 *
 * BEST PRACTICES:
 * - Usa le varianti Bootstrap: 'primary', 'secondary', 'danger', 'outline-primary', ecc.
 * - Usa le dimensioni Bootstrap: 'sm', 'md', 'lg'
 * - Supporta icone dal registry SVG_ICONS o SVG custom
 * - Gestisce automaticamente stati disabled e loading
 * - Utilizza ng-content per il testo del bottone
 *
 * ESEMPIO D'USO:
 * <piao-button variant="primary" size="md" (clicked)="handleClick()">
 *   Conferma
 * </piao-button>
 */
@Component({
  selector: 'piao-button',
  imports: [CommonModule],
  templateUrl: './button.component.html',
  styleUrl: './button.component.scss',
})
export class ButtonComponent {
  /** Testo alternativo/tooltip per il bottone */
  @Input() title?: string;

  /** Testo all'interno del button */
  @Input() label?: string;

  /** Disabilita il bottone */
  @Input() disabled = false;

  /** Mostra spinner di caricamento */
  @Input() loading = false;

  /**
   * Variante del bottone secondo Bootstrap Italia
   * Varianti disponibili:
   * - Solid: 'primary', 'secondary', 'danger', 'success', 'warning', 'info'
   * - Outline: 'primary-outline', 'secondary-outline', 'danger-outline'
   * - Modal: 'primary-modal', 'primary-outline-modal'
   */
  @Input() variant:
    | 'primary'
    | 'secondary'
    | 'danger'
    | 'success'
    | 'warning'
    | 'info'
    | 'primary-outline'
    | 'secondary-outline'
    | 'danger-outline'
    | 'primary-modal'
    | 'primary-outline-modal'
    | string = 'primary';

  /**
   * Dimensione del bottone secondo Bootstrap
   * - 'sm': Small button
   * - 'md': Default/Medium button
   * - 'lg': Large button
   * - 'lg-plus': Extra large button (custom)
   * - 'md-plus': Medium+ button (custom)
   */
  @Input() size: 'sm' | 'md' | 'lg' | 'lg-plus' | 'md-plus' | string = 'md';

  /**
   * Icona da mostrare nel bottone.
   * Può essere:
   * - Un nome di icona dal registry (es. 'close', 'menu', 'user')
   * - Una stringa SVG custom
   */
  @Input() icon?: IconName | string;

  /** Posizione dell'icona ('left' o 'right') */
  @Input() iconPosition: 'left' | 'right' = 'left';

  /** Dimensione dell'icona (default: icona di dimensione standard) */
  @Input() iconSize: string = 'icon-standard';

  /** Indica se il bottone contiene solo un'icona (senza testo) */
  @Input() iconOnly: boolean = false;

  /** Classi CSS personalizzate da aggiungere al button */
  @Input() customClass?: string;

  /** Evento emesso al click */
  @Output() clicked = new EventEmitter<void>();

  constructor(private sanitizer: DomSanitizer) {}

  /**
   * Handler click: emette evento solo se non disabilitato o in loading.
   */
  onButtonClick(): void {
    if (!this.disabled && !this.loading) {
      this.clicked.emit();
    }
  }

  /**
   * Ottiene l'SVG sanitizzato dell'icona.
   * Se l'icona è un nome del registry, lo recupera automaticamente.
   */
  getSanitizedIcon(): SafeHtml | null {
    if (!this.icon) {
      return null;
    }

    // Prova a ottenere l'icona dal registry
    let iconSvg = getIcon(this.icon as IconName);

    // Se non è nel registry, usa la stringa passata direttamente
    if (!iconSvg) {
      iconSvg = this.icon;
    }

    // Sanitizza l'SVG per la sicurezza
    return this.sanitizer.bypassSecurityTrustHtml(iconSvg);
  }

  /**
   * Verifica se l'icona deve essere mostrata
   */
  hasIcon(): boolean {
    return !!this.icon;
  }

  /**
   * Ottiene le classi CSS per l'icona
   */
  getIconClasses(): string {
    return this.iconSize;
  }

  /**
   * Ottiene le classi CSS per il bottone utilizzando Bootstrap Italia
   * Genera classi secondo le convenzioni di Bootstrap 5.2.3
   */
  getButtonClasses(): string {
    const classes: string[] = ['btn'];

    // Aggiunge le classi di variante Bootstrap
    const variantClass = this.getBootstrapVariantClass();
    if (variantClass) {
      classes.push(variantClass);
    }

    // Aggiunge le classi di dimensione Bootstrap
    const sizeClass = this.getBootstrapSizeClass();
    if (sizeClass) {
      classes.push(sizeClass);
    }

    // Aggiunge classi per gestire icone
    if (this.hasIcon() || this.loading) {
      classes.push('d-inline-flex', 'align-items-center', 'gap-2');
    }

    // Aggiunge classe per bottone icon-only
    if (this.iconOnly) {
      classes.push('btn-icon');
    }

    // Aggiunge classi custom se fornite
    if (this.customClass) {
      classes.push(this.customClass);
    }

    return classes.join(' ');
  }

  /**
   * Mappa le varianti personalizzate alle classi Bootstrap
   */
  private getBootstrapVariantClass(): string {
    switch (this.variant) {
      case 'primary':
      case 'primary-modal':
        return 'btn-primary';

      case 'secondary':
        return 'btn-secondary';

      case 'danger':
        return 'btn-danger';

      case 'success':
        return 'btn-success';

      case 'warning':
        return 'btn-warning';

      case 'info':
        return 'btn-info';

      case 'primary-outline':
      case 'primary-outline-modal':
        return 'btn-outline-primary';

      case 'secondary-outline':
        return 'btn-outline-secondary';

      case 'danger-outline':
        return 'btn-outline-danger';

      default:
        // Se è una classe Bootstrap diretta (es. 'btn-accent')
        if (this.variant.startsWith('btn-')) {
          return this.variant;
        }
        return 'btn-primary';
    }
  }

  /**
   * Mappa le dimensioni personalizzate alle classi Bootstrap
   */
  private getBootstrapSizeClass(): string {
    switch (this.size) {
      case 'sm':
        return 'btn-sm';

      case 'lg':
      case 'lg-plus':
        return 'btn-lg';

      case 'md':
      case 'md-plus':
      default:
        // Bootstrap usa dimensione media come default (nessuna classe necessaria)
        return '';
    }
  }
}
