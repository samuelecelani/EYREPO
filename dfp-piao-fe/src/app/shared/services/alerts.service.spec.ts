import { TestBed } from '@angular/core/testing';

import { AlertsService } from './alerts.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { NotificaService } from './notifica.service';

describe('AlertsService', () => {
  let service: AlertsService;
  let httpMock: HttpTestingController;

  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AlertsService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificaService, value: mockNotificationService },
      ]
    });
    service = TestBed.inject(AlertsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
