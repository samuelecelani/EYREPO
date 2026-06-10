import { Component, Input } from '@angular/core';
import { IconComponent } from '../../components/icon/icon.component';
import { BadgeComponent, TBadge } from '../../components/badge/badge.component';
import { RouterLink } from '@angular/router';
import { SvgComponent } from '../../components/svg/svg.component';
import { SharedModule } from '../../module/shared/shared.module';

export interface INewsDocument {
  id: string;
  nome: string;
  tipo: string;
  sizeMb: number;
  downloadUrl: string;
}

export interface INews {
  id: number | string;
  type: 'CIRCOLARE' | 'NOVITA' | 'ALTRO';
  icon: string;
  badgeText: string;
  badgeVariant: TBadge['variant'];
  title: string;
  titleLink: string;
  description: string;
  date: string;
  dateIso: string;
  readMoreLink: string;
  content: string;
  imageBase64?: string | null;
  keywords: string[];
  documents: INewsDocument[];
}

@Component({
  selector: 'piao-news',
  imports: [SharedModule, IconComponent, SvgComponent, BadgeComponent, RouterLink],
  templateUrl: './news.component.html',
  styleUrl: './news.component.scss',
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
}
