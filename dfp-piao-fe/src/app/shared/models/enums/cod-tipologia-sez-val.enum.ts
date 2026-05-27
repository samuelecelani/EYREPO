export enum CodTipologiaSezValEnum {
  PIAO = 'PIAO',
  SEZ1 = 'SEZIONE_1',
  SEZ2_1 = 'SEZIONE_21',
  SEZ2_2 = 'SEZIONE_22',
  SEZ2_3 = 'SEZIONE_23',
  SEZ3_1 = 'SEZIONE_31',
  SEZ3_2 = 'SEZIONE_32',
  SEZ3_3_1 = 'SEZIONE_331',
  SEZ3_3_2 = 'SEZIONE_332',
  SEZ4 = 'SEZIONE_4',
}

export function getSezioneLabel(codTipologia: CodTipologiaSezValEnum, additional: string): string {
  console.log('getSezioneLabel - codTipologia:', codTipologia, 'additional:', additional);
  switch (codTipologia) {
    case CodTipologiaSezValEnum.PIAO:
      return 'PIAO ' + additional;
    case CodTipologiaSezValEnum.SEZ1:
      return 'Sezione 1';
    case CodTipologiaSezValEnum.SEZ2_1:
      return 'Sotto-sezione 2.1';
    case CodTipologiaSezValEnum.SEZ2_2:
      return 'Sotto-sezione 2.2';
    case CodTipologiaSezValEnum.SEZ2_3:
      return 'Sotto-sezione 2.3';
    case CodTipologiaSezValEnum.SEZ3_1:
      return 'Sotto-sezione 3.1';
    case CodTipologiaSezValEnum.SEZ3_2:
      return 'Sotto-sezione 3.2';
    case CodTipologiaSezValEnum.SEZ3_3_1:
      return 'Sotto-sezione 3.3.1';
    case CodTipologiaSezValEnum.SEZ3_3_2:
      return 'Sotto-sezione 3.3.2';
    case CodTipologiaSezValEnum.SEZ4:
      return 'Sezione 4';
    default:
      return '';
  }
}
