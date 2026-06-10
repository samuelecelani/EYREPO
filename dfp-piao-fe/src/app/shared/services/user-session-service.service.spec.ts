import { TestBed } from '@angular/core/testing';

import { UserSessionServiceService } from './user-session-service.service';

describe('UserSessionServiceService', () => {
  let service: UserSessionServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserSessionServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
