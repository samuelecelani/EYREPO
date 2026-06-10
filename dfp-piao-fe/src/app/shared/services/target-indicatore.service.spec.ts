import { TestBed } from '@angular/core/testing';

import { TargetIndicatoreService } from './target-indicatore.service';

describe('TargetIndicatoreService', () => {
  let service: TargetIndicatoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TargetIndicatoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
