import { TestBed } from '@angular/core/testing';

import { MotivazioneDichiarazioneService } from './motivazione-dichiarazione.service';

describe('MotivazioneDichiarazioneService', () => {
  let service: MotivazioneDichiarazioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MotivazioneDichiarazioneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
