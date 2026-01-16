import { Component } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared.module';
import { ProfiloUtenteComponent } from '../../../../shared/ui/gestionale/profilo-utente/profilo-utente.component';
@Component({
  selector: 'piao-dettaglio-attivita',
  standalone: true,
  imports: [SharedModule, ProfiloUtenteComponent],
  templateUrl: './dettaglio-attivita.component.html',
  styleUrls: ['./dettaglio-attivita.component.css'],
})
export class DettaglioAttivitaComponent {}
