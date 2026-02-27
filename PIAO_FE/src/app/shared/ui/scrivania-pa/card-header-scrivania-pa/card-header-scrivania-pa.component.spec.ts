import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';

import { CardHeaderScrivaniaPAComponent } from './card-header-scrivania-pa.component';
import { of, Subject } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { routes } from '../../../../app.routes';
import { NotificaService } from '../../../services/notifica.service';
import { mockUtente } from '../../../utils/mock';

describe('CardHeaderScrivaniaPaComponent', () => {
  let component: CardHeaderScrivaniaPAComponent;
  let fixture: ComponentFixture<CardHeaderScrivaniaPAComponent>;
  let mockRouter: any;

  let mockAccountService: any;
  let mockAlerts: any;

  const mockTranslate = jasmine.createSpyObj('TranslateService', ['get', 'stream']);

  // Metodi principali
  mockTranslate.get.and.callFake((key: string) => of(`mocked: ${key}`));
  mockTranslate.stream.and.callFake((key: string) => of(`mocked: ${key}`));

  // Eventi che il pipe usa
  mockTranslate.onLangChange = new Subject();
  mockTranslate.onTranslationChange = new Subject();
  mockTranslate.onDefaultLangChange = new Subject();
  mockTranslate.onFallbackLangChange = new Subject();

  mockAccountService = jasmine.createSpyObj('AccountService', ['getAccount', 'getFunzionalita']);

  mockAlerts = jasmine.createSpyObj('AlertsService', ['getAllAlters']);

  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications',
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigateByUrl']);
    mockAccountService.getAccount.and.returnValue(of(mockUtente));
    mockAccountService.getFunzionalita.and.returnValue(of(['TEST', 'ALTRO']));

    await TestBed.configureTestingModule({
      imports: [CardHeaderScrivaniaPAComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        { provide: NotificaService, useValue: mockNotificationService },
        { provide: TranslateService, useValue: mockTranslate },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CardHeaderScrivaniaPAComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('goToAlerts', () => {
    component.handleGoToAlerts();
    expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('pages/servizi-piao');
  });

  it('getVisibilty', fakeAsync(() => {
    component.ngOnInit();
    tick();
    expect(component.isVisible).toEqual(false);
  }));
});
