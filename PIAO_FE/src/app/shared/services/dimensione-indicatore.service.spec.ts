import { TestBed } from '@angular/core/testing';

import { DimensioneIndicatoreService } from './dimensione-indicatore.service';

describe('DimensioneIndicatoreService', () => {
  let service: DimensioneIndicatoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DimensioneIndicatoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
