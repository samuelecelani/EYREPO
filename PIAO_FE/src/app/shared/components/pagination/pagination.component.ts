import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Atomic Pagination Component
 * Componente atomico e riutilizzabile per la paginazione di array di dati.
 *
 * Features:
 * - Navigazione con chevron left/right
 * - Mostra i numeri delle pagine al centro
 * - Completamente configurabile tramite Input
 * - Emette eventi per la gestione della pagina corrente
 *
 * BEST PRACTICES:
 * - Il componente NON gestisce i dati, solo la logica della paginazione
 * - Il parent component deve gestire lo slicing dell'array
 * - Usa @Input() per configurare totalItems e itemsPerPage
 * - Usa @Output() per ricevere l'evento di cambio pagina
 */
@Component({
    selector: 'piao-pagination',
    imports: [CommonModule],
    templateUrl: './pagination.component.html',
    styleUrl: './pagination.component.scss'
})
export class PaginationComponent implements OnChanges {
  /** Numero totale di elementi */
  @Input() totalItems: number = 0;

  /** Numero di elementi per pagina */
  @Input() itemsPerPage: number = 10;

  /** Pagina corrente (1-based) */
  @Input() currentPage: number = 1;

  /** Evento emesso quando cambia la pagina */
  @Output() pageChange = new EventEmitter<number>();

  /** Numero totale di pagine calcolato */
  totalPages: number = 0;

  /** Array di numeri pagina da mostrare (max 5 alla volta) */
  pageNumbers: number[] = [];

  /** Numero massimo di pagine da mostrare contemporaneamente */
  private maxVisiblePages = 5;

  ngOnChanges(changes: SimpleChanges): void {
    // Ricalcola quando cambiano gli input
    this.calculatePagination();
  }

  /**
   * Calcola il numero totale di pagine e genera l'array dei numeri (max 5 visibili)
   */
  private calculatePagination(): void {
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);

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
