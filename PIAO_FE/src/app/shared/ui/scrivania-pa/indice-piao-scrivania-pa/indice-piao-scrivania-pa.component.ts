import { Component, inject, OnInit } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { DropdownComponent } from '../../../components/dropdown/dropdown.component';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { PIAOService } from '../../../services/piao.service';
import { LabelValue } from '../../../models/interfaces/label-value';
import { StructureIndicePiaoComponent } from '../../structure-indice-piao/structure-indice-piao.component';
import { BaseComponent } from '../../../components/base/base.component';
import { filter, map, of, switchMap, take } from 'rxjs';
import { GraphicIndicePiaoComponent } from './graphic-indice-piao/graphic-indice-piao.component';
import { StrutturaIndicePiaoDTO } from '../../../models/classes/struttura-indice-piao-dto';
import { SectionStatusEnum } from '../../../models/enums/section-status.enum';

@Component({
  selector: 'piao-indice-piao-scrivania-pa',
  imports: [
    SharedModule,
    DropdownComponent,
    StructureIndicePiaoComponent,
    GraphicIndicePiaoComponent,
  ],
  templateUrl: './indice-piao-scrivania-pa.component.html',
  styleUrl: './indice-piao-scrivania-pa.component.scss',
})
export class IndicePiaoScrivaniaPAComponent extends BaseComponent implements OnInit {
  piaoService: PIAOService = inject(PIAOService);

  piaoDTO!: PIAODTO;

  dropdown: LabelValue[] = [];
  resPiaoDTO: PIAODTO[] = [];
  sezioni: StrutturaIndicePiaoDTO[] = [];

  statusSezioniGrapich!: number[];

  titleDropdown: string = 'SCRIVANIA_PA.INDICE_PIAO_CARD.DROPDOWN.TITLE';

  ngOnInit(): void {
    this.loadFunzionalita();
    this.funzionalita$
      .pipe(
        filter(() => !!this.paRiferimento?.codePA),
        take(1),
        switchMap(() => this.piaoService.getAllPiao(this.paRiferimento.codePA)),
        switchMap((getAllPiao) => {
          if (getAllPiao != null && getAllPiao.length > 0) {
            return this.piaoService.getStructureIndicePIAO(getAllPiao[0].id || -1).pipe(
              map((structure) => ({
                getAllPiao,
                structure,
              }))
            );
          }
          return of().pipe(
            map((structure) => ({
              getAllPiao,
              structure,
            }))
          );
        })
      )
      .subscribe({
        next: (res) => {
          console.log(res);
          if (res.getAllPiao != null && res.getAllPiao.length > 0) {
            this.dropdown = res.getAllPiao.map((x) => ({
              label: x.denominazione || '',
              value: x.denominazione,
            }));

            this.resPiaoDTO = res.getAllPiao;
            this.piaoDTO = this.resPiaoDTO[0];

            this.sezioni = res.structure;

            console.log(this.sezioni);

            this.getStatusSezioni();
          }
        },
      });
  }

  changePiao(event: any) {
    console.log(event);
    this.piaoDTO = this.resPiaoDTO.find((x) => x.denominazione === event) || this.piaoDTO;
    console.log(this.piaoDTO);
    this.piaoService.getStructureIndicePIAO(this.piaoDTO!.id || -1).subscribe({
      next: (res) => {
        this.sezioni = res;
        this.getStatusSezioni();
      },
    });
  }

  getStatusSezioni() {
    //stati fissi
    const fixedStates = [
      SectionStatusEnum.COMPILATA,
      SectionStatusEnum.IN_VALIDAZIONE,
      SectionStatusEnum.VALIDATA,
      SectionStatusEnum.DA_COMPILARE,
      SectionStatusEnum.IN_COMPILAZIONE,
    ];

    const record: Record<string, number> = {};

    //riempio il record con gli stati e lo 0 come valore
    fixedStates.forEach((state) => (record[state] = 0));

    const visit = (n: StrutturaIndicePiaoDTO) => {
      //se ha children, il parent non viene calcolato
      const hasChildren = Array.isArray(n.children) && n.children.length > 0;

      if (hasChildren) {
        n.children!.forEach(visit);
      } else {
        //se lo stato è null lo metto in da compilare
        const key = n.statoSezione ?? SectionStatusEnum.DA_COMPILARE;
        if (record[key] !== undefined) {
          record[key]++;
        }
      }
    };

    console.log(this.sezioni);
    console.log(record);

    this.sezioni.forEach(visit);
    this.statusSezioniGrapich = Object.values(record);
  }
}
