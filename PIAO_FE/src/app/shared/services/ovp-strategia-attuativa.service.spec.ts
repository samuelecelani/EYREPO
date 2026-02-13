import { TestBed } from '@angular/core/testing';

import { OvpStrategiaAttuativaService } from './ovp-strategia-attuativa.service';

describe('OvpStrategiaAttuativaService', () => {
  let service: OvpStrategiaAttuativaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OvpStrategiaAttuativaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
