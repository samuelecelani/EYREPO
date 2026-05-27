import { TestBed } from '@angular/core/testing';

import { ApprovazioneService } from './approvazione.service';

describe('ApprovazioneService', () => {
  let service: ApprovazioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApprovazioneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
