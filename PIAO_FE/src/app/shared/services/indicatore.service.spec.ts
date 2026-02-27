import { TestBed } from '@angular/core/testing';
import { IndicatoreService } from './indicatore.service';

describe('IndicatoreService', () => {
  let service: IndicatoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(IndicatoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
