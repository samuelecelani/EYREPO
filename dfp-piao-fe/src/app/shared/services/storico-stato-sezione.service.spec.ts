import { TestBed } from '@angular/core/testing';

import { StoricoStatoSezioneService } from './storico-stato-sezione.service';

describe('StoricoStatoSezioneService', () => {
  let service: StoricoStatoSezioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StoricoStatoSezioneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
