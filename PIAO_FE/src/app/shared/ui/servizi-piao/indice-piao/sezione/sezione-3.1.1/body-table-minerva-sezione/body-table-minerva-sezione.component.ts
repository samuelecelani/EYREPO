import { Component, inject, Input } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { TableMinervaComponent } from '../../../../../../components/table-minerva/table-minerva.component';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { ModalNotFoundDataMinervaComponent } from '../modal-not-found-data-minerva/modal-not-found-data-minerva.component';
import { SHAPE_ICON } from '../../../../../../utils/constants';
import { MinervaService } from '../../../../../../services/minerva.service';
import { map } from 'rxjs';

@Component({
    selector: 'piao-body-table-minerva-sezione',
    imports: [SharedModule, TableMinervaComponent, ModalComponent, ModalNotFoundDataMinervaComponent],
    templateUrl: './body-table-minerva-sezione.component.html',
    styleUrl: './body-table-minerva-sezione.component.scss'
})
export class BodyTableMinervaSezioneComponent {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() isMock: boolean = false;

  showTable: boolean = false;

  openModal: boolean = false;

  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  dataTable: any;

  minervaService: MinervaService = inject(MinervaService);

  handleOpenTable() {
    if (this.isMock) {
      this.minervaService
        .getTabellaMock()
        .pipe(map((res) => res.data))
        .subscribe({
          next: (res: any) => {
            if (res) {
              this.dataTable = res;
              this.showTable = true;
              this.openModal = false;
            } else {
              this.showTable = false;
              this.openModal = true;
            }
          },
        });
    } else {
      this.openModal = true;
    }
  }

  handleCloseModal() {
    this.openModal = false;
  }
}
