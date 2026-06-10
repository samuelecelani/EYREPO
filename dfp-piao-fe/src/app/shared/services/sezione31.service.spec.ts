import { TestBed } from '@angular/core/testing';

import { Sezione31Service } from './sezione31.service';

describe('Sezione31Service', () => {
  let service: Sezione31Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Sezione31Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
