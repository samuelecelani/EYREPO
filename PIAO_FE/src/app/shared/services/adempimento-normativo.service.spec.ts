import { TestBed } from '@angular/core/testing';

import { AdempimentoNormativoService } from './adempimento-normativo.service';

describe('AdempimentoNormativoService', () => {
  let service: AdempimentoNormativoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AdempimentoNormativoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
