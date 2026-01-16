import { TestBed } from '@angular/core/testing';

import { Sezione1Service } from './sezioni-1.service';

describe('Sezione1Service', () => {
  let service: Sezione1Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Sezione1Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
