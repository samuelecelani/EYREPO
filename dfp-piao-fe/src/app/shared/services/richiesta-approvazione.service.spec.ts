import { TestBed } from '@angular/core/testing';

import { RichiestaApprovazioneService } from './richiesta-approvazione.service';

describe('RichiestaApprovazioneService', () => {
  let service: RichiestaApprovazioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RichiestaApprovazioneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
