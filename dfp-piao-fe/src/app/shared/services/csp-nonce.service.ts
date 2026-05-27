import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class CspNonceService {
  private nonce: string | null = null;
  private observer: MutationObserver | null = null;

  constructor(@Inject(PLATFORM_ID) private platformId: object) {}

  /**
   * Inizializza il servizio leggendo il nonce dal meta tag
   * e attivando il MutationObserver per applicare il nonce agli stili dinamici
   */
  init(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    // Leggi il nonce dal meta tag iniettato da nginx
    const metaTag = document.querySelector('meta[property="csp-nonce"]');
    if (metaTag) {
      this.nonce = metaTag.getAttribute('content');
    }

    if (!this.nonce) {
      console.warn(
        'CSP nonce non trovato nel meta tag. Gli stili inline potrebbero essere bloccati.'
      );
      return;
    }

    // PRIMA: Patcha i metodi DOM per intercettare gli stili inline PRIMA che vengano bloccati dalla CSP
    this.patchDomMethods();

    // Applica il nonce a tutti gli stili inline esistenti
    this.applyNonceToExistingStyles();

    // Attiva il MutationObserver per intercettare nuovi stili dinamici
    this.startObserver();
  }

  /**
   * Patcha i metodi DOM per intercettare le operazioni di stile inline
   * PRIMA che vengano bloccate dalla CSP
   */
  private patchDomMethods(): void {
    if (!this.nonce) return;

    const nonce = this.nonce;

    // Salva i metodi originali
    const originalSetAttribute = Element.prototype.setAttribute;
    const originalCreateElement = document.createElement.bind(document);

    // Patcha setAttribute per intercettare gli attributi style
    Element.prototype.setAttribute = function (this: Element, name: string, value: string): void {
      if (name.toLowerCase() === 'style' && value && this instanceof HTMLElement) {
        // Invece di impostare l'attributo style, crea un elemento style con nonce
        if (!this.id) {
          this.id = 'csp-' + Math.random().toString(36).substring(2, 11);
        }

        const styleElement = document.createElement('style');
        styleElement.setAttribute('nonce', nonce);
        styleElement.textContent = `#${this.id} { ${value} }`;
        document.head.appendChild(styleElement);
        return;
      }

      // Per tutti gli altri attributi, usa il metodo originale
      return originalSetAttribute.call(this, name, value);
    };

    // Patcha createElement per aggiungere automaticamente nonce agli elementi style
    document.createElement = function (
      tagName: string,
      options?: ElementCreationOptions
    ): HTMLElement {
      const element = originalCreateElement(tagName, options);

      if (tagName.toLowerCase() === 'style' && element instanceof HTMLStyleElement) {
        element.setAttribute('nonce', nonce);
      }

      return element;
    } as any;

    // Patcha CSSStyleDeclaration per intercettare le modifiche dirette a element.style
    const originalStyleSetProperty = CSSStyleDeclaration.prototype.setProperty;
    CSSStyleDeclaration.prototype.setProperty = function (
      this: CSSStyleDeclaration,
      property: string,
      value: string | null,
      priority?: string
    ): void {
      // Per ora lasciamo passare le modifiche dirette allo style
      // La CSP le bloccherà, ma è difficile intercettarle tutte
      return originalStyleSetProperty.call(this, property, value, priority);
    };
  }

  /**
   * Applica il nonce a tutti gli elementi style inline esistenti
   * e converte gli attributi style inline in elementi style con nonce
   */
  private applyNonceToExistingStyles(): void {
    if (!this.nonce) return;

    // Applica nonce ai tag <style> esistenti
    const styleElements = document.querySelectorAll('style:not([nonce])');
    styleElements.forEach((style) => {
      style.setAttribute('nonce', this.nonce!);
    });

    // Converti tutti gli attributi style inline esistenti
    const elementsWithStyle = document.querySelectorAll('[style]');
    elementsWithStyle.forEach((element) => {
      if (element instanceof HTMLElement) {
        this.convertInlineStylesToStyleElement(element);
      }
    });
  }

  /**
   * Avvia il MutationObserver per intercettare nuovi elementi style aggiunti al DOM
   */
  private startObserver(): void {
    if (!this.nonce || !isPlatformBrowser(this.platformId)) return;

    this.observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'childList') {
          mutation.addedNodes.forEach((node) => {
            // Controlla se il nodo aggiunto è un elemento style
            if (node.nodeName === 'STYLE' && node instanceof HTMLElement) {
              if (!node.hasAttribute('nonce')) {
                node.setAttribute('nonce', this.nonce!);
              }
            }

            // Controlla anche i figli del nodo aggiunto
            if (node instanceof HTMLElement) {
              const childStyles = node.querySelectorAll('style:not([nonce])');
              childStyles.forEach((style) => {
                style.setAttribute('nonce', this.nonce!);
              });

              // Converti attributi style inline in elementi style con nonce
              this.convertInlineStylesToStyleElements(node);
            }
          });
        }

        // Intercetta anche modifiche agli attributi style inline
        if (mutation.type === 'attributes' && mutation.attributeName === 'style') {
          const target = mutation.target as HTMLElement;
          if (target.style.length > 0) {
            // Converti l'attributo style inline in un elemento style con nonce
            this.convertInlineStylesToStyleElement(target);
          }
        }
      });
    });

    // Osserva tutto il documento
    this.observer.observe(document.documentElement, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['style'],
    });
  }

  /**
   * Converte gli attributi style inline di un elemento in un elemento style con nonce
   */
  private convertInlineStylesToStyleElement(element: HTMLElement): void {
    if (!this.nonce || !element.hasAttribute('style')) return;

    const inlineStyle = element.getAttribute('style');
    if (!inlineStyle) return;

    // Genera un ID univoco per l'elemento se non ce l'ha
    if (!element.id) {
      element.id = 'csp-' + Math.random().toString(36).substring(2, 11);
    }

    // Crea un elemento style con il nonce
    const styleElement = document.createElement('style');
    styleElement.setAttribute('nonce', this.nonce);
    styleElement.textContent = `#${element.id} { ${inlineStyle} }`;

    // Rimuovi l'attributo style inline
    element.removeAttribute('style');

    // Aggiungi l'elemento style al document head
    document.head.appendChild(styleElement);
  }

  /**
   * Converte tutti gli attributi style inline nei discendenti di un nodo
   */
  private convertInlineStylesToStyleElements(node: HTMLElement): void {
    const elementsWithStyle = node.querySelectorAll('[style]');
    elementsWithStyle.forEach((element) => {
      if (element instanceof HTMLElement) {
        this.convertInlineStylesToStyleElement(element);
      }
    });
  }

  /**
   * Ferma il MutationObserver
   */
  destroy(): void {
    if (this.observer) {
      this.observer.disconnect();
      this.observer = null;
    }
  }

  /**
   * Restituisce il nonce corrente
   */
  getNonce(): string | null {
    return this.nonce;
  }
}
