import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeaderComponent } from './header.component';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { routes } from '../../../app.routes';
import { WINDOW } from '../../config/window-config';
import { NotificaService } from '../../services/notifica.service';

describe('Header2Component', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let mockRouter: any;
  let windowStub: Window & typeof globalThis;


  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    windowStub = {
      location: { assign(value: string) { return value } },
    } as unknown as Window & typeof globalThis

    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        { provide: NotificaService, useValue: mockNotificationService },
        { provide: Router, useValue: mockRouter },
        { provide: WINDOW, useValue: windowStub }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
