import { TestBed } from '@angular/core/testing';

import { OvpStrategiaIndicatoreService } from './ovp-strategia-indicatore.service';

describe('OvpStrategiaIndicatoreService', () => {
  let service: OvpStrategiaIndicatoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OvpStrategiaIndicatoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
