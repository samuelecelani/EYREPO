export enum FaseEnum {
  PIANIFICAZIONE = 'Pianificazione delle performance',
  VALUTAZIONE = 'Valutazione delle performance',
  MONITORAGGIO = 'Monitoraggio delle performance',
  AGGIUNGI_NUOVA = 'Aggiungi nuova delle performance',
}

export function isFaseValue(value: string): boolean {
  return Object.values(FaseEnum).includes(value as FaseEnum);
}

export function isFase(value: string, fase: FaseEnum): boolean {
  return value === fase;
}
