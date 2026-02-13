import { TestBed } from '@angular/core/testing';

import { EventoRischioService } from './evento-rischio.service';

describe('EventoRischioService', () => {
  let service: EventoRischioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EventoRischioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
