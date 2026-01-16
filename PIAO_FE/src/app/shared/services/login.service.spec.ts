import { TestBed } from '@angular/core/testing';

import { LoginService } from './login.service';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { AppConfig } from '../models/interfaces/config.model';
import { NotificaService } from './notifica.service';

describe('LoginService', () => {
  let service: LoginService;
  let httpMock: HttpTestingController;

  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  const mockConfig: AppConfig = {
    appName: 'PIAO',
    apiBaseUrl: 'http://localhost:9082',
    oauth: { clientId: 'spa-public-client' },
    requestTimeoutMs: 8000,
    retry: { maxAttempts: 3, baseDelayMs: 300 }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificaService, useValue: mockNotificationService }
      ]
    });

    service = TestBed.inject(LoginService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
