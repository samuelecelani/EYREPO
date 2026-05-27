import { Component, inject, OnInit } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { DropdownComponent } from '../../../components/dropdown/dropdown.component';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { PIAOService } from '../../../services/piao.service';
import { LabelValue } from '../../../models/interfaces/label-value';
import { StructureIndicePiaoComponent } from '../../structure-indice-piao/structure-indice-piao.component';
import { BaseComponent } from '../../../components/base/base.component';
import { map, of, switchMap, take } from 'rxjs';
import { GraphicIndicePiaoComponent } from './graphic-indice-piao/graphic-indice-piao.component';
import { StrutturaIndicePiaoDTO } from '../../../models/classes/struttura-indice-piao-dto';
import { SectionStatusEnum } from '../../../models/enums/section-status.enum';
import { TipologiaOnline } from '../../../models/enums/tipologia-online.enum';

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

  isSemplificato: boolean = false;

  ngOnInit(): void {
    this.getUserContext$()
      .pipe(
        take(1),
        switchMap(({ paRiferimento }) => this.piaoService.getAllPiao(paRiferimento.codePA)),
        switchMap((getAllPiao) => {
          if (getAllPiao != null && getAllPiao.length > 0) {
            // Ordino subito per versione così il PIAO "di default" è coerente
            // tra dropdown, piaoDTO e struttura caricata per il grafico.
            const allPiaoSort = [...getAllPiao].sort((a, b) =>
              (a.versione ?? '').localeCompare(b.versione ?? '')
            );
            return this.piaoService.getStructureIndicePIAO(allPiaoSort[0].id || -1).pipe(
              map((structure) => ({
                getAllPiao: allPiaoSort,
                structure,
              }))
            );
          }
          return of({ getAllPiao, structure: [] as StrutturaIndicePiaoDTO[] });
        })
      )
      .subscribe({
        next: (res) => {
          console.log(res);
          if (res.getAllPiao != null && res.getAllPiao.length > 0) {
            const allPiaoSort = res.getAllPiao;
            this.dropdown = allPiaoSort.map((x) => ({
              label: x.denominazione + ' - ' + x.versione || '',
              value: x.denominazione + '-' + x.versione,
            }));

            this.resPiaoDTO = allPiaoSort;
            this.piaoDTO = allPiaoSort[0];
            this.isSemplificato = this.piaoDTO.tipologiaOnline === TipologiaOnline.SEMPLIFICATO;

            if (res.structure && res.structure.length > 0) {
              let piaoSection = res.structure.find((section) => section.numeroSezione === '0');
              let approvazioneSection = res.structure.find(
                (section) => section.numeroSezione === '5'
              );
              this.sezioni = res.structure.filter(
                (section) => section !== piaoSection && section !== approvazioneSection
              );
            }

            console.log(this.sezioni);

            this.getStatusSezioni();
          }
        },
      });
  }

  changePiao(event: any) {
    console.log(event);
    this.piaoDTO =
      this.resPiaoDTO.find((x) => x.denominazione + '-' + x.versione === event) || this.piaoDTO;
    console.log(this.piaoDTO);
    this.piaoService.getStructureIndicePIAO(this.piaoDTO!.id || -1).subscribe({
      next: (res) => {
        if (res && res.length > 0) {
          let piaoSection = res.find((section) => section.numeroSezione === '0');
          let approvazioneSection = res.find((section) => section.numeroSezione === '5');
          this.sezioni = res.filter(
            (section) => section !== piaoSection && section !== approvazioneSection
          );
        }
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
      SectionStatusEnum.RICHIESTA_APPROVAZIONE,
      SectionStatusEnum.PUBBLICATO,
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
