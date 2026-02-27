import { TestBed } from '@angular/core/testing';

import { PIAOService } from './piao.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { NotificaService } from './notifica.service';

describe('PIAOService', () => {
  let service: PIAOService;

  let httpMock: HttpTestingController;


  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        PIAOService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificaService, value: mockNotificationService },
      ]
    }).compileComponents();
    service = TestBed.inject(PIAOService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
