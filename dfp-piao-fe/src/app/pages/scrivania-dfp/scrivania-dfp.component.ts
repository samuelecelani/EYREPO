import { Component } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared.module';
import { CardHeaderScrivaniaDfpComponent } from '../../shared/ui/scrivania-dfp/card-header-scrivania-dfp/card-header-scrivania-dfp.component';
import { AvanzamentoAmministrazioniComponent } from '../../shared/ui/scrivania-dfp/avanzamento-amministrazioni/avanzamento-amministrazioni.component';
import { AvvisiScrivaniaDFPComponent } from '../../shared/ui/scrivania-dfp/avvisi-scrivania-dfp/avvisi-scrivania-dfp.component';

@Component({
  selector: 'piao-scrivania-dfp',
  imports: [
    SharedModule,
    CardHeaderScrivaniaDfpComponent,
    AvanzamentoAmministrazioniComponent,
    AvvisiScrivaniaDFPComponent,
  ],
  templateUrl: './scrivania-dfp.component.html',
  styleUrl: './scrivania-dfp.component.scss',
})
export class ScrivaniaDfpComponent {}
