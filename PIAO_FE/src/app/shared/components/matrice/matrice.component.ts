import { Component, Input } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { ItemMatrice } from '../../models/interfaces/item-matrice';
import { CardAlertComponent } from '../../ui/card-alert/card-alert.component';
import { TooltipComponent } from '../tooltip/tooltip.component';

@Component({
  selector: 'piao-matrice',
  standalone: true,
  imports: [SharedModule, CardAlertComponent, TooltipComponent],
  templateUrl: './matrice.component.html',
  styleUrls: ['./matrice.component.scss'],
})
export class MatriceComponent {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() subTitleAlert!: string;
  @Input() iconAlert!: string;
  @Input() titleAlert!: string;

  column: string[] = [
    'DG UDM PNRR',
    'DG Ordinamenti',
    'Istituzioni',
    'DG Ricerca',
    'DG Personale, bilancio e servizi strumentali',
  ];

  matrice: ItemMatrice[] = [
    {
      politicalPriority: "Potenziamento dell'offerta formativa",
      organisationalAreas: {
        'DG UDM PNRR': [
          { code: 'VP1', description: 'desczione1' },
          { code: 'VP2', description: 'desczione2' },
          { code: 'VP3', description: 'desczione3' },
        ],
        'DG Ordinamenti': [],
        Istituzioni: [
          { code: 'VP1', description: 'desczione1' },
          { code: 'VP2', description: 'desczione2' },
          { code: 'VP3', description: 'desczione3' },
        ],
        'DG Ricerca': [],
        'DG Personale, bilancio e servizi strumentali': [],
      },
    },
    {
      politicalPriority: 'Digitalizzazione del patrimonio culturale',
      organisationalAreas: {
        'DG UDM PNRR': [],
        'DG Ordinamenti': [{ code: 'VP4', description: 'desczione4' }],
        Istituzioni: [],
        'DG Ricerca': [],
        'DG Personale, bilancio e servizi strumentali': [],
      },
    },
    {
      politicalPriority: 'Digitalizzazione delle infrastrutture di ricerca',
      organisationalAreas: {
        'DG UDM PNRR': [],
        'DG Ordinamenti': [],
        Istituzioni: [],
        'DG Ricerca': [],
        'DG Personale, bilancio e servizi strumentali': [
          { code: 'VP9', description: 'desczione9' },
        ],
      },
    },
  ];
}
