import { Component, inject, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { TableMinervaComponent } from '../../../../../../components/table-minerva/table-minerva.component';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { ModalNotFoundDataMinervaComponent } from '../modal-not-found-data-minerva/modal-not-found-data-minerva.component';
import { SHAPE_ICON } from '../../../../../../utils/constants';
import { MinervaService } from '../../../../../../services/minerva.service';
import { FormControl } from '@angular/forms';
import { TipologiaTabellaSezione331 } from '../../../../../../models/enums/tipologia-tabella-sezione3-3-1.enum';

@Component({
  selector: 'piao-body-table-minerva-sezione',
  imports: [SharedModule, TableMinervaComponent, ModalComponent, ModalNotFoundDataMinervaComponent],
  templateUrl: './body-table-minerva-sezione.component.html',
  styleUrl: './body-table-minerva-sezione.component.scss',
})
export class BodyTableMinervaSezioneComponent implements OnInit, OnChanges {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() isMinerva: boolean = false;
  @Input() isRequired: boolean = false;
  @Input() formControlTable!: FormControl;
  @Input() typeTable!: string;
  @Input() tipologiaTabellaSezione331!: TipologiaTabellaSezione331;
  @Input() tabelleMinerva!: any[];
  @Input() isDettaglio: boolean = false;

  showTable!: boolean;

  openModal: boolean = false;

  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  dataTable: any;

  minervaService: MinervaService = inject(MinervaService);

  ngOnInit(): void {
    this.handleOpenTable(false);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tabelleMinerva'] && !changes['tabelleMinerva'].firstChange) {
      this.handleOpenTable(false);
    }
  }

  handleFillTable(openModalMinerva: boolean) {
    if (this.tabelleMinerva && this.tabelleMinerva.length > 0) {
      const tabella = this.tabelleMinerva.find(
        (tab) => tab['name'] === this.tipologiaTabellaSezione331.toString()
      );
      if (tabella && tabella['rows'] && tabella['rows'].length > 0) {
        this.dataTable = tabella;
        this.showTable = true;
        this.openModal = false;
        this.formControlTable.setValue(true, { emitEvent: false });
      } else {
        this.showTable = false;
        if (openModalMinerva) {
          this.openModal = true;
        } else {
          this.openModal = false;
        }
        this.formControlTable.setValue(null, { emitEvent: false });
      }
    } else {
      this.showTable = false;
      if (openModalMinerva) {
        this.openModal = true;
      } else {
        this.openModal = false;
      }
      this.formControlTable.setValue(null, { emitEvent: false });
    }
  }

  handleOpenTable(openModalMinerva: boolean) {
    if (this.isMinerva) {
      this.handleFillTable(openModalMinerva);
    } else {
      if (openModalMinerva) {
        this.openModal = true;
      } else {
        this.openModal = false;
      }
      this.formControlTable.setValue(null, { emitEvent: false });
    }
  }

  handleCloseModal() {
    this.openModal = false;
  }
}
