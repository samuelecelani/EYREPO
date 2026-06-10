import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Page, Pageable } from '../../models/interfaces/pageable';

/**
 * Atomic Pagination Component
 * Componente atomico e riutilizzabile per la paginazione di array di dati.
 *
 * Supporta DUE modalità (mutuamente esclusive):
 *
 * 1. CLIENT-SIDE (default, retro-compatibile):
 *    Il parent fornisce `totalItems` / `itemsPerPage` / `currentPage` e ascolta `(pageChange)`
 *    per fare lo slicing locale dell'array.
 *
 * 2. SERVER-SIDE (opzionale):
 *    Il parent passa `[page]="page"` con la risposta Spring `Page<T>` ricevuta dal BE.
 *    In questa modalità `totalItems`, `itemsPerPage` e `currentPage` vengono derivati
 *    automaticamente da `page` e il componente emette `(pageableChange)` con il nuovo
 *    `Pageable` da inviare al BE per la pagina richiesta.
 */
@Component({
  selector: 'piao-pagination',
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginationComponent implements OnChanges {
  /** Numero totale di elementi (modalità client-side). */
  @Input() totalItems: number = 0;

  /** Numero di elementi per pagina (modalità client-side). */
  @Input() itemsPerPage: number = 10;

  /** Pagina corrente (1-based). */
  @Input() currentPage: number = 1;

  /**
   * (Opzionale) Pagina Spring Data ricevuta dal BE.
   * Se valorizzata, ATTIVA la modalità server-side:
   * - totalItems, itemsPerPage e currentPage vengono derivati automaticamente
   * - viene emesso anche `pageableChange` ad ogni cambio pagina
   */
  @Input() page: Page<unknown> | null = null;

  /** Evento emesso quando cambia la pagina (1-based). Sempre presente. */
  @Output() pageChange = new EventEmitter<number>();

  /**
   * Evento emesso solo in modalità server-side: contiene il `Pageable`
   * da inviare al BE per richiedere la nuova pagina.
   */
  @Output() pageableChange = new EventEmitter<Pageable>();

  /** Numero totale di pagine calcolato */
  totalPages: number = 0;

  /** Array di numeri pagina da mostrare (max 5 alla volta) */
  pageNumbers: number[] = [];

  /** Numero massimo di pagine da mostrare contemporaneamente */
  private maxVisiblePages = 5;

  ngOnChanges(changes: SimpleChanges): void {
    // Se è stato passato un Page<T>, deriva gli input dal server response
    if (this.page) {
      this.totalItems = this.page.totalElements ?? 0;
      this.itemsPerPage = this.page.size ?? this.itemsPerPage;
      // Spring Page.number è 0-based, il componente lavora 1-based
      this.currentPage = (this.page.number ?? 0) + 1;
    }
    // Ricalcola quando cambiano gli input
    this.calculatePagination();
  }

  // ...existing code...

  /**
   * Calcola il numero totale di pagine e genera l'array dei numeri (max 5 visibili)
   */
  private calculatePagination(): void {
    const perPage = this.itemsPerPage > 0 ? this.itemsPerPage : 1;
    this.totalPages = Math.ceil(this.totalItems / perPage);

    // Assicurati che la pagina corrente sia valida
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages || 1;
    }
    if (this.currentPage < 1) {
      this.currentPage = 1;
    }

    // Calcola quali pagine mostrare (max 5)
    this.pageNumbers = this.getVisiblePages();
  }

  /**
   * Calcola le pagine visibili (max 5 alla volta)
   */
  private getVisiblePages(): number[] {
    if (this.totalPages <= this.maxVisiblePages) {
      // Se ci sono 5 o meno pagine totali, mostra tutte
      return Array.from({ length: this.totalPages }, (_, i) => i + 1);
    }

    // Calcola il range di pagine da mostrare
    let startPage = Math.max(1, this.currentPage - Math.floor(this.maxVisiblePages / 2));
    let endPage = startPage + this.maxVisiblePages - 1;

    // Aggiusta se siamo vicini alla fine
    if (endPage > this.totalPages) {
      endPage = this.totalPages;
      startPage = Math.max(1, endPage - this.maxVisiblePages + 1);
    }

    return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
  }

  /**
   * Naviga alla prima pagina
   */
  firstPage(): void {
    if (this.currentPage !== 1) {
      this.goToPage(1);
    }
  }

  /**
   * Naviga all'ultima pagina
   */
  lastPage(): void {
    if (this.currentPage !== this.totalPages) {
      this.goToPage(this.totalPages);
    }
  }

  /**
   * Naviga alla pagina precedente
   */
  previousPage(): void {
    if (this.currentPage > 1) {
      this.goToPage(this.currentPage - 1);
    }
  }

  /**
   * Naviga alla pagina successiva
   */
  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.goToPage(this.currentPage + 1);
    }
  }

  /**
   * Naviga a una pagina specifica
   */
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.pageChange.emit(this.currentPage);
      // In modalità server-side emette anche il Pageable da inviare al BE
      if (this.page) {
        this.pageableChange.emit({
          page: this.currentPage - 1, // Spring Pageable è 0-based
          size: this.itemsPerPage,
        });
      }
    }
  }

  /**
   * Verifica se la pagina precedente è disponibile
   */
  hasPreviousPage(): boolean {
    return this.currentPage > 1;
  }

  /**
   * Verifica se la pagina successiva è disponibile
   */
  hasNextPage(): boolean {
    return this.currentPage < this.totalPages;
  }

  /**
   * Verifica se una pagina è quella corrente
   */
  isCurrentPage(page: number): boolean {
    return page === this.currentPage;
  }
}
