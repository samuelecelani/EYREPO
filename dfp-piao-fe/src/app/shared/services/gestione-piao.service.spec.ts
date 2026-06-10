import { TestBed } from '@angular/core/testing';

import { GestionePiaoService } from './gestione-piao.service';

describe('GestionePiaoService', () => {
  let service: GestionePiaoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GestionePiaoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
