import { TestBed } from '@angular/core/testing';

import { Sezione331Service } from './sezione331.service';

describe('Sezione331Service', () => {
  let service: Sezione331Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Sezione331Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
