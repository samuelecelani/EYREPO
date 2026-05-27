import { TestBed } from '@angular/core/testing';

import { MisurePrevenzioneService } from './misure-prevenzione.service';

describe('MisurePrevenzioneService', () => {
  let service: MisurePrevenzioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MisurePrevenzioneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
