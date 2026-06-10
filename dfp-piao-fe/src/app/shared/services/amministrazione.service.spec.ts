import { TestBed } from '@angular/core/testing';

import { AmministrazioneService } from './amministrazione.service';

describe('AnagraficaService', () => {
  let service: AmministrazioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AmministrazioneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
