import { TestBed } from '@angular/core/testing';

import { ObiettivoPrevenzioneCorruzioneTrasparenzaService } from './obiettivo-prevenzione-corruzione-trasparenza.service';

describe('ObiettivoPrevenzioneCorruzioneTrasparenzaService', () => {
  let service: ObiettivoPrevenzioneCorruzioneTrasparenzaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ObiettivoPrevenzioneCorruzioneTrasparenzaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
