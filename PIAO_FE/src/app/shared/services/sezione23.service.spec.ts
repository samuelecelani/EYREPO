import { TestBed } from '@angular/core/testing';

import { Sezione23Service } from './sezione23.service';

describe('Sezione23Service', () => {
  let service: Sezione23Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Sezione23Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
