import { TestBed } from '@angular/core/testing';

import { MisuraPrevenzioneService } from './misura-prevenzione.service';

describe('MisuraPrevenzioneService', () => {
  let service: MisuraPrevenzioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MisuraPrevenzioneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
