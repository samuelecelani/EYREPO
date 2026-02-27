import { Component, Input } from '@angular/core';
import { IconComponent } from '../../components/icon/icon.component';
import { BadgeComponent, TBadge } from '../../components/badge/badge.component';
import { RouterLink } from '@angular/router';
import { SvgComponent } from '../../components/svg/svg.component';
import { SharedModule } from '../../module/shared/shared.module';

export interface INews {
  id: number | string;
  icon: string;
  badgeText: string;
  badgeVariant: TBadge['variant'];
  title: string;
  titleLink: string;
  description: string;
  date: string;
  readMoreLink: string;
  // TODO: Aggiungere dal backend quando disponibile
  // dataCreazione?: Date | string; // Data di creazione per ordinamento
  // inEvidenza?: boolean; // Flag per evidenziare la novità
}

@Component({
    selector: 'piao-news',
    imports: [SharedModule, IconComponent, SvgComponent, BadgeComponent, RouterLink],
    templateUrl: './news.component.html',
    styleUrl: './news.component.css'
})
//TODO: Refactor
export class NewsComponent {
  @Input() icon: string = 'megaphone';
  @Input() badgeText: string = '';
  @Input() badgeVariant: 'primary' | 'success' | 'secondary' = 'primary'; // | 'warning' | 'danger' | 'info'
  @Input() title: string = '';
  @Input() titleLink: string = '#';
  @Input() description: string = '';
  @Input() date: string = '';
  @Input() readMoreLink: string = '#';

  // @Input() news: INews
}
