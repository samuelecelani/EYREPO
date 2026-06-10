import { TestBed } from '@angular/core/testing';

import { DichiarazioneScadenzaService } from './dichiarazione-scadenza.service';

describe('DichiarazioneScadenzaService', () => {
  let service: DichiarazioneScadenzaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DichiarazioneScadenzaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
