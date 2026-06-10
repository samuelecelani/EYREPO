import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SharedModule } from '../../../../shared/module/shared/shared.module';
import { ProfiloUtenteComponent } from '../../../../shared/ui/gestionale/profilo-utente/profilo-utente.component';

@Component({
  selector: 'piao-gestione-profilo-utente',
  standalone: true,
  imports: [SharedModule, ProfiloUtenteComponent],
  templateUrl: './gestione-profilo-utente.component.html',
  styleUrls: ['./gestione-profilo-utente.component.css'],
})
export class GestioneProfiloUtenteComponent {
  private activatedRoute = inject(ActivatedRoute);
  userId = this.activatedRoute.snapshot.params['id'];
}
