import { TestBed } from '@angular/core/testing';

import { ModalitaMonitoraggioService } from './modalita-monitoraggio.service';

describe('ModalitaMonitoraggioService', () => {
  let service: ModalitaMonitoraggioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ModalitaMonitoraggioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
