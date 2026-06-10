import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SharedModule } from '../../../module/shared/shared.module';
import { ButtonComponent } from '../../../components/button/button.component';
import { SvgComponent } from '../../../components/svg/svg.component';

export interface StatCardData {
  titleKey: string;
  rows: { labelKey: string; value: string }[];
  ctaLabelKey?: string;
  ctaAction?: () => void;
}

@Component({
  selector: 'piao-avanzamento-amministrazioni',
  imports: [SharedModule, ButtonComponent, SvgComponent],
  templateUrl: './avanzamento-amministrazioni.component.html',
  styleUrl: './avanzamento-amministrazioni.component.scss',
})
export class AvanzamentoAmministrazioniComponent {
  private router = inject(Router);

  // Mock data - da sostituire con dati dal servizio
  cards: StatCardData[] = [
    {
      titleKey: 'SCRIVANIA_DFP.AVANZAMENTO.CARD_1.TITLE',
      rows: [
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.N_AMMINISTRAZIONI', value: '00' },
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.DI_CUI_PAC', value: '00' },
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.DI_CUI_PAL', value: '00' },
      ],
    },
    {
      titleKey: 'SCRIVANIA_DFP.AVANZAMENTO.CARD_2.TITLE',
      rows: [
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.N_AMMINISTRAZIONI', value: '00 di 00' },
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.DI_CUI_PAC', value: '00' },
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.DI_CUI_PAL', value: '00' },
      ],
    },
    {
      titleKey: 'SCRIVANIA_DFP.AVANZAMENTO.CARD_3.TITLE',
      rows: [
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.N_AMMINISTRAZIONI', value: '00 di 00' },
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.DI_CUI_PAC', value: '00' },
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.DI_CUI_PAL', value: '00' },
      ],
    },
    {
      titleKey: 'SCRIVANIA_DFP.AVANZAMENTO.CARD_4.TITLE',
      rows: [
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.N_AMMINISTRAZIONI', value: '00 di 00' },
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.CARD_4.MANDATO_FORM', value: '00' },
        { labelKey: 'SCRIVANIA_DFP.AVANZAMENTO.CARD_4.NON_MANDATO_FORM', value: '00' },
      ],
      ctaLabelKey: 'SCRIVANIA_DFP.AVANZAMENTO.VEDI_STORICO',
    },
  ];

  anno = 2025;

  goToSollecita(): void {
    this.router.navigate(['/solleciti']);
  }

  goToStorico(): void {
    this.router.navigate(['/storico-dichiarazioni']);
  }
}
