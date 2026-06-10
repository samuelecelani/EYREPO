import { TestBed } from '@angular/core/testing';
import { ScaricoMassivoService } from './scarico-massivo.service';

describe('ScaricoMassivoService', () => {
  let service: ScaricoMassivoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ScaricoMassivoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
