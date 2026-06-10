import { TestBed } from '@angular/core/testing';

import { Sezione332Service } from './sezione332.service';

describe('Sezione332Service', () => {
  let service: Sezione332Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Sezione332Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
