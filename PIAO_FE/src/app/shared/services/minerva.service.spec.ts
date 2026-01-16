import { TestBed } from '@angular/core/testing';

import { MinervaService } from './minerva.service';

describe('MinervaService', () => {
  let service: MinervaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MinervaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
