import { TestBed } from '@angular/core/testing';

import { PrincipiGuidaService } from './principi-guida.service';

describe('PrincipiGuidaService', () => {
  let service: PrincipiGuidaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PrincipiGuidaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
