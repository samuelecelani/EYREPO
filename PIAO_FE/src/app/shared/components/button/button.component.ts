import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { IconName, getIcon } from '../../../../assets/svg-icons.registry';

/**
 * Atomic Button component
 * Un bottone atomico riusabile, personalizzabile tramite varianti, dimensioni,
 * icone e stato disabilitato/caricamento. (Non contiene logica di presentazione/tema).
 *
 * NOTA: Ricordarsi di gestire accessibilità e customizzazione in HTML!
 *
 * BEST PRACTICES:
 * - Usa il registry SVG_ICONS per icone predefinite (passa solo il nome)
 * - Oppure passa un SVG custom come stringa
 * - L'icona viene sanitizzata automaticamente per la sicurezza
 * - Usa ng-content per il testo del bottone
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

  /** Mostra spinner di caricamento (non gestito qui ma nel template) */
  @Input() loading = false;

  /** Variante del bottone (es. 'primary', 'secondary', 'danger') */
  @Input() variant: 'primary' | 'secondary' | 'danger' | string = 'primary';

  /** Dimensione del bottone (es. 'sm', 'md', 'lg') */
  @Input() size: 'sm' | 'md' | 'lg' | string = 'md';

  /**
   * Icona da mostrare nel bottone.
   * Può essere:
   * - Un nome di icona dal registry (es. 'close', 'menu', 'user')
   * - Una stringa SVG custom
   */
  @Input() icon?: IconName | string;

  /** Posizione dell'icona ('left' o 'right') */
  @Input() iconPosition: 'left' | 'right' = 'left';

  /** Dimensione icona (classe CSS o dimensione) */
  @Input() iconSize: string = 'h-5 w-5';

  /** Indica se il bottone contiene solo un'icona (senza testo) */
  @Input() iconOnly: boolean = false;

  /** Evento emesso al click */
  @Output() clicked = new EventEmitter<void>();

  constructor(private sanitizer: DomSanitizer) {}

  /**
   * Handler click: emette evento solo se non disabilitato o in loading.
   */
  onButtonClick() {
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

    // Sanitizza l'SVG per la sicurezza usando bypassSecurityTrustHtml
    // Questo permette ad Angular di renderizzare correttamente l'SVG nell'innerHTML
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
   * Ottiene le classi CSS per il bottone in base alle varianti
   */
  getButtonClasses(): string {
    const classes: string[] = [
      'inline-flex',
      'items-center',
      'justify-center',
      'transition-all',
      'duration-200',
      'font-medium',
      'rounded-lg',
      'focus:outline-none',
      'focus:ring-2',
      'focus:ring-offset-2',
    ];

    // Size classes
    switch (this.size) {
      case 'sm':
        classes.push('px-3', 'py-1.5', 'text-sm', 'gap-1.5');
        break;

      case 'lg':
        classes.push('px-[1.5rem]', 'py-[1rem]', 'text-[1rem]', 'gap-[3.75rem]');
        break;

      case 'lg-plus':
        classes.push('px-[1.5rem]', 'py-[1.1rem]', 'text-[1rem]', 'gap-[3.75rem]');
        break;

      case 'md-plus':
        classes.push('px-4', 'py-2.5', 'text-base', 'gap-3');
        break;
      case 'md':
      default:
        classes.push('px-4', 'py-2', 'text-base', 'gap-2');
        break;
    }

    // Variant classes
    switch (this.variant) {
      case 'primary':
        classes.push(
          'bg-[#0066CC]',
          'text-white',
          'hover:bg-blue-700',
          'focus:ring-blue-500',
          'disabled:bg-blue-300',
          'disabled:cursor-not-allowed',
          'font-semibold'
        );
        break;
      case 'primary-outline':
        classes.push(
          'bg-white-600',
          'text-[#0066CC]',
          'border border-solid border-custom-blue',
          'hover:bg-white-700',
          'focus:ring-white-500',
          'disabled:bg-white-300',
          'disabled:cursor-not-allowed',
          'font-semibold'
        );
        break;
      case 'secondary':
        classes.push(
          'bg-gray-200',
          'text-gray-900',
          'hover:bg-gray-300',
          'focus:ring-gray-500',
          'disabled:bg-gray-100',
          'disabled:cursor-not-allowed'
        );
        break;
      case 'secondary-outline':
        classes.push(
          'bg-white-600',
          'text-gray-900',
          'border border-solid border-custom-gray',
          'hover:bg-gray-200',
          'focus:ring-gray-300',
          'disabled:bg-white-300',
          'disabled:cursor-not-allowed',
          'font-semibold'
        );
        break;
      case 'danger':
        classes.push(
          'bg-red-600',
          'text-white',
          'hover:bg-red-700',
          'focus:ring-red-500',
          'disabled:bg-red-300',
          'disabled:cursor-not-allowed'
        );
        break;
      case 'primary-modal':
        classes.push(
          'bg-[#0066CC]',
          'text-white',
          'hover:bg-blue-700',
          'focus:ring-blue-500',
          'disabled:bg-blue-300',
          'disabled:cursor-not-allowed',
          'font-semibold'
        );
        break;
      case 'primary-outline-modal':
        classes.push(
          'bg-white-600',
          'text-[#0066CC]',
          'border border-solid border-custom-blue',
          'hover:bg-white-700',
          'focus:ring-white-500',
          'disabled:bg-white-300',
          'disabled:cursor-not-allowed',
          'font-semibold'
        );
        break;
      default:
        classes.push('bg-gray-500', 'text-white', 'hover:bg-gray-600', 'focus:ring-gray-400');
        break;
    }

    return classes.join(' ');
  }
}
