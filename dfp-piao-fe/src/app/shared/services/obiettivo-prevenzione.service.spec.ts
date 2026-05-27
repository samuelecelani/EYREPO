import { TestBed } from '@angular/core/testing';

import { ObiettivoPrevenzioneService } from './obiettivo-prevenzione.service';

describe('ObiettivoPrevenzioneService', () => {
  let service: ObiettivoPrevenzioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ObiettivoPrevenzioneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
