import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { ItemMatrice } from '../../models/interfaces/item-matrice';
import { CardAlertComponent } from '../../ui/card-alert/card-alert.component';
import { TooltipComponent } from '../tooltip/tooltip.component';
import { Sezione1Service } from '../../services/sezioni-1.service';
import { ItemMatriceDTO } from '../../models/classes/item-matrice-dto';
import { ModalComponent } from '../modal/modal.component';
import { SHAPE_ICON } from '../../utils/constants';
import { OvpItemDTO } from '../../models/classes/ovp-item-dto';

@Component({
  selector: 'piao-matrice',
  standalone: true,
  imports: [SharedModule, CardAlertComponent, TooltipComponent, ModalComponent],
  templateUrl: './matrice.component.html',
  styleUrls: ['./matrice.component.scss'],
})
export class MatriceComponent implements OnInit {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() subTitleAlert!: string;
  @Input() iconAlert!: string;
  @Input() titleAlert!: string;
  @Input() idSezione21!: number;
  @Input() idSezione1!: number;
  @Input() idPiao!: number;

  sezione1Service: Sezione1Service = inject(Sezione1Service);

  openModalDetails: boolean = false;
  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';
  selectedVp: OvpItemDTO | null = null;

  matrice!: ItemMatriceDTO[];
  column: string[] = [];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    if (!this.idSezione1) {
      console.warn('idSezioni o idPiao non definito, skip caricamento matrice');
      return;
    }

    this.sezione1Service
      .getItemMatrice(this.idSezione21, this.idSezione1, this.idPiao)
      .subscribe((data) => {
        this.matrice = data;
        console.log('Matrice caricata:', this.matrice);

        // Estrai tutte le chiavi uniche di organisationalAreas da tutti gli elementi
        const allKeys = new Set<string>();
        this.matrice.forEach((item) => {
          if (item.organisationalAreas) {
            Object.keys(item.organisationalAreas).forEach((key) => allKeys.add(key));
          }
        });
        this.column = Array.from(allKeys);
      });
  }

  openDettaglioModal(data: OvpItemDTO): void {
    console.log('openDettaglioModal chiamato con dati:', data);
    this.selectedVp = data;
    this.openModalDetails = true;
  }

  closeModal(): void {
    this.openModalDetails = false;
    this.selectedVp = null;
  }
}
