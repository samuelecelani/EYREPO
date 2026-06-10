import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { DichiarazioneScadenzaService } from '../../../shared/services/dichiarazione-scadenza.service';
import { StoricoDichiarazioneDFPDTO } from '../../../shared/models/classes/storico-dichiarazione-dfp-dto';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'piao-storico-dichiarazioni',
  imports: [SharedModule, PaginationComponent, DatePipe],
  templateUrl: './storico-dichiarazioni.component.html',
  styleUrl: './storico-dichiarazioni.component.scss',
})
export class StoricoDichiarazioniComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly dichiarazioneScadenzaService = inject(DichiarazioneScadenzaService);

  readonly itemsPerPage = 4;
  readonly currentPage = signal(1);

  private readonly allRows = signal<StoricoDichiarazioneDFPDTO[]>([]);

  readonly totalItems = computed(() => this.allRows().length);

  readonly paginatedRows = computed(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    return this.allRows().slice(start, start + this.itemsPerPage);
  });

  ngOnInit(): void {
    this.loadStorico();
  }

  private loadStorico(): void {
    this.dichiarazioneScadenzaService.findAllaStoricoDichiarazioneScadenza().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.allRows.set(data),
      error: (err) => console.error('Errore caricamento storico dichiarazioni:', err),
    });
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  goToDettaglio(row: StoricoDichiarazioneDFPDTO): void {
    const navigate = () => {
      this.router.navigate(
        ['/storico-dichiarazioni/dettaglio-mancata-compilazione', row.id],
        {
          state: { amministrazione: row.amministrazione, codePA: row.codePA },
        }
      );
    };

    if (row.stato === false && row.id != null) {
      this.dichiarazioneScadenzaService.updateStatoDichiarazioneScadenza(row.id, true).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          row.stato = true;
          navigate();
        },
        error: (err) => {
          console.error('Errore aggiornamento stato dichiarazione:', err);
          navigate();
        },
      });
    } else {
      navigate();
    }
  }
}
