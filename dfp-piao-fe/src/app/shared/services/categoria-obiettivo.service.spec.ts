import { TestBed } from '@angular/core/testing';

import { CategoriaObiettivoService } from './categoria-obiettivo.service';

describe('CategoriaObiettivoService', () => {
  let service: CategoriaObiettivoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CategoriaObiettivoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
