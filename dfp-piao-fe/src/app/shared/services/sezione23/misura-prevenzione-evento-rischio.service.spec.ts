import { TestBed } from '@angular/core/testing';

import { MisuraPrevenzioneEventoRischioService } from './misura-prevenzione-evento-rischio.service';

describe('MisuraPrevenzioneEventoRischioService', () => {
  let service: MisuraPrevenzioneEventoRischioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MisuraPrevenzioneEventoRischioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
