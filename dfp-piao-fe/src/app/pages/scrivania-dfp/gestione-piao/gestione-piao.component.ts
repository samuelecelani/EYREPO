import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { AssetService } from '../../../shared/services/asset.service';

interface CardActionGestionePiao {
  id: string;
  title: string;
  subTitle: string;
  icon: string;
  path: string;
}

@Component({
  selector: 'dfp-gestione-piao',
  imports: [SharedModule, ButtonComponent, IconComponent],
  templateUrl: './gestione-piao.component.html',
  styleUrl: './gestione-piao.component.scss',
})
export class GestionePiaoComponent {
  private router: Router = inject(Router);
  protected readonly asset = inject(AssetService);

  cardAction: CardActionGestionePiao[] = [
    {
      id: 'RICERCA',
      title: 'SCRIVANIA_DFP.GESTIONE_PIAO.CARD_ACTIONS.RICERCA.TITLE',
      subTitle: 'SCRIVANIA_DFP.GESTIONE_PIAO.CARD_ACTIONS.RICERCA.SUB_TITLE',
      icon: 'Search',
      path: '/gestione-piao/revisione',
    },
    {
      id: 'SCARICO_MASSIVO',
      title: 'SCRIVANIA_DFP.GESTIONE_PIAO.CARD_ACTIONS.SCARICO_MASSIVO.TITLE',
      subTitle: 'SCRIVANIA_DFP.GESTIONE_PIAO.CARD_ACTIONS.SCARICO_MASSIVO.SUB_TITLE',
      icon: 'Download',
      path: '/gestione-piao/scarico-massivo',
    },
  ];

  handleGoToPath(card: CardActionGestionePiao): void {
    this.router.navigateByUrl(card.path);
  }

  trackByCardId(_index: number, card: CardActionGestionePiao): string {
    return card.id;
  }
}
