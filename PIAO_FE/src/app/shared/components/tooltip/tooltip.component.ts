import {
  Component,
  Input,
  ViewChild,
  TemplateRef,
  ElementRef,
  EventEmitter,
  Output,
} from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SvgComponent } from '../svg/svg.component';
import { Overlay, OverlayModule, OverlayRef, ConnectedPosition } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { ViewContainerRef } from '@angular/core';

@Component({
  selector: 'piao-tooltip',
  imports: [SharedModule, SvgComponent, OverlayModule],
  templateUrl: './tooltip.component.html',
  styleUrl: './tooltip.component.scss',
})
export class TooltipComponent {
  @Input() descTooltip!: string;
  @Input() icon!: string;
  @Input() isInnerHTML: boolean = false;
  @Input() keepOpenOnHover: boolean = false;
  @Input() translateParams: any = {};
  @Input() dataContext: any = null;
  @Input() hrefInTooltip: boolean = false;
  @Output() tooltipContentClick: EventEmitter<any> = new EventEmitter<any>();

  @ViewChild('tooltipTemplate') tooltipTemplate!: TemplateRef<any>;
  @ViewChild('trigger') trigger!: ElementRef;

  private overlayRef: OverlayRef | null = null;
  private scrollListener: (() => void) | null = null;
  private isMouseOverTooltip: boolean = false;
  private isMouseOverTrigger: boolean = false;
  private hideTimeout: any = null;
  private clickableElement: HTMLElement | null = null;
  private clickHandler: ((event: MouseEvent) => void) | null = null;

  // Variabile statica per tenere traccia del tooltip attualmente aperto
  private static currentOpenTooltip: TooltipComponent | null = null;

  constructor(
    private overlay: Overlay,
    private viewContainerRef: ViewContainerRef
  ) {}

  showTooltip(): void {
    if (this.hideTimeout) {
      clearTimeout(this.hideTimeout);
      this.hideTimeout = null;
    }

    this.isMouseOverTrigger = true;

    if (this.overlayRef) {
      return;
    }

    // Chiudi il tooltip precedentemente aperto
    if (TooltipComponent.currentOpenTooltip && TooltipComponent.currentOpenTooltip !== this) {
      TooltipComponent.currentOpenTooltip.closeTooltip();
    }

    // Imposta questo tooltip come quello attualmente aperto
    TooltipComponent.currentOpenTooltip = this;

    const positions: ConnectedPosition[] = [
      {
        originX: 'center',
        originY: 'top',
        overlayX: 'end',
        overlayY: 'bottom',
        offsetY: -12,
      },
    ];

    const positionStrategy = this.overlay
      .position()
      .flexibleConnectedTo(this.trigger)
      .withPositions(positions)
      .withPush(false)
      .withFlexibleDimensions(false)
      .withGrowAfterOpen(false);

    this.overlayRef = this.overlay.create({
      positionStrategy,
      scrollStrategy: this.overlay.scrollStrategies.close(),
      hasBackdrop: false,
      panelClass: 'tooltip-overlay-panel',
    });

    const portal = new TemplatePortal(this.tooltipTemplate, this.viewContainerRef);
    this.overlayRef.attach(portal);

    // Aggiungi listener per click sull'intero overlay per intercettare click su .tooltip-link
    setTimeout(() => {
      if (this.overlayRef && this.hrefInTooltip) {
        const overlayElement = this.overlayRef.overlayElement;

        console.log("Aggiungo listener SULL'INTERO OVERLAY");

        // Listener sull'overlay intero invece che sull'elemento specifico
        this.clickHandler = ((event: MouseEvent) => {
          const target = event.target as HTMLElement;
          console.log("Click sull'overlay, target:", target.tagName, target.className);

          // Cerca tutti i P.tooltip-link nell'overlay
          const tooltipLinks = overlayElement.querySelectorAll('p.tooltip-link');
          console.log('Elementi p.tooltip-link trovati:', tooltipLinks.length);

          // Per ogni link, controlla se il click è avvenuto dentro il suo bounding box
          for (let i = 0; i < tooltipLinks.length; i++) {
            const link = tooltipLinks[i] as HTMLElement;
            const rect = link.getBoundingClientRect();

            const isClickInsideLink =
              event.clientX >= rect.left &&
              event.clientX <= rect.right &&
              event.clientY >= rect.top &&
              event.clientY <= rect.bottom;

            if (isClickInsideLink) {
              console.log('Dati da emettere:', this.dataContext);
              event.stopPropagation();
              event.preventDefault();
              this.tooltipContentClick.emit(this.dataContext || this.translateParams);
              this.closeTooltip();
              return;
            }
          }

          console.log('Click su overlay ma NON su P.tooltip-link');
        }).bind(this);

        overlayElement.addEventListener('click', this.clickHandler, true);
        console.log("✓ Listener aggiunto all'overlay");
      }
    }, 200);

    // Chiudi il tooltip su qualsiasi scroll
    this.scrollListener = () => {
      this.hideTooltip();
    };
    window.addEventListener('scroll', this.scrollListener, true);
  }

  hideTooltip(): void {
    this.isMouseOverTrigger = false;

    if (this.keepOpenOnHover || this.hrefInTooltip) {
      // Aspetta un po' prima di chiudere per dare tempo al mouse di spostarsi sul tooltip
      this.hideTimeout = setTimeout(() => {
        if (!this.isMouseOverTooltip && !this.isMouseOverTrigger) {
          this.closeTooltip();
        }
      }, 100);
    } else {
      this.closeTooltip();
    }
  }

  onTooltipMouseEnter(): void {
    if (this.keepOpenOnHover || this.hrefInTooltip) {
      this.isMouseOverTooltip = true;
      if (this.hideTimeout) {
        clearTimeout(this.hideTimeout);
        this.hideTimeout = null;
      }
    }
  }

  onTooltipMouseLeave(): void {
    if (this.keepOpenOnHover || this.hrefInTooltip) {
      this.isMouseOverTooltip = false;
      this.hideTimeout = setTimeout(() => {
        if (!this.isMouseOverTooltip && !this.isMouseOverTrigger) {
          this.closeTooltip();
        }
      }, 300);
    }
  }

  onTooltipContentClick(event: Event): void {
    let target = event.target as HTMLElement;

    console.log('=== CLICK NEL TOOLTIP ===');
    console.log('Target cliccato:', target);
    console.log('CurrentTarget:', event.currentTarget);

    // Verifica se l'elemento cliccato ha l'attributo data-clickable o un suo discendente
    let clickedElement = target;
    let foundClickableElement = false;
    let depth = 0;

    // Cerca verso l'alto fino a trovare data-clickable="true"
    while (clickedElement && clickedElement !== event.currentTarget) {
      depth++;
      console.log(
        `Livello ${depth}:`,
        clickedElement.tagName,
        'Classi:',
        clickedElement.className,
        'data-clickable:',
        clickedElement.getAttribute('data-clickable')
      );

      if (clickedElement.getAttribute('data-clickable') === 'true') {
        console.log('✓ TROVATO elemento clickable!');
        foundClickableElement = true;
        break;
      }
      clickedElement = clickedElement.parentElement as HTMLElement;
    }

    console.log('foundClickableElement:', foundClickableElement);

    // Emetti l'evento SOLO se hai trovato l'elemento clickable
    if (foundClickableElement) {
      console.log('Emetto evento con dati:', this.dataContext);
      event.stopPropagation();
      event.preventDefault();
      this.tooltipContentClick.emit(this.dataContext || this.translateParams);
      this.closeTooltip();
    } else {
      console.log('✗ Click NON su elemento clickable, ignoro');
    }
  }

  private closeTooltip(): void {
    if (this.scrollListener) {
      window.removeEventListener('scroll', this.scrollListener, true);
      this.scrollListener = null;
    }
    if (this.overlayRef && this.clickHandler) {
      console.log("Rimuovo listener dall'overlay");
      this.overlayRef.overlayElement.removeEventListener('click', this.clickHandler, true);
      this.clickHandler = null;
    }
    if (this.overlayRef) {
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
    this.isMouseOverTooltip = false;
    this.isMouseOverTrigger = false;
    this.clickableElement = null;

    // Resetta il tooltip attualmente aperto se è questo
    if (TooltipComponent.currentOpenTooltip === this) {
      TooltipComponent.currentOpenTooltip = null;
    }
  }
}
