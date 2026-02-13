import { TestBed } from '@angular/core/testing';

import { AttivitaSensibileRischioService } from './attivita-sensibile-rischio.service';

describe('AttivitaSensibileRischioService', () => {
  let service: AttivitaSensibileRischioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AttivitaSensibileRischioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
