import { Component } from '@angular/core';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { CardHeaderGestionaleComponent } from '../../../shared/ui/gestionale/card-header-gestionale/card-header-gestionale.component';
import { ElencoAttivitaComponent } from '../../../shared/ui/gestionale/elenco-attivita/elenco-attivita.component';

@Component({
  selector: 'app-gestionale',
  imports: [SharedModule, CardHeaderGestionaleComponent, ElencoAttivitaComponent],
  templateUrl: './gestionale.component.html',
  styleUrls: ['./gestionale.component.scss'],
})
export class GestionaleComponent {}
