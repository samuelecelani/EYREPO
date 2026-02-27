import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiziPiaoComponent } from './servizi-piao.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateService } from '@ngx-translate/core';
import { of, Subject } from 'rxjs';
import { provideRouter } from '@angular/router';
import { routes } from '../../../app.routes';
import { NotificaService } from '../../../shared/services/notifica.service';

describe('ServiziPiaoComponent', () => {
  let component: ServiziPiaoComponent;
  let fixture: ComponentFixture<ServiziPiaoComponent>;

  const mockTranslate = jasmine.createSpyObj('TranslateService', ['get', 'stream']);

  // Metodi principali
  mockTranslate.get.and.callFake((key: string) => of(`mocked: ${key}`));
  mockTranslate.stream.and.callFake((key: string) => of(`mocked: ${key}`));

  // Eventi che il pipe usa
  mockTranslate.onLangChange = new Subject();
  mockTranslate.onTranslationChange = new Subject();
  mockTranslate.onDefaultLangChange = new Subject();
  mockTranslate.onFallbackLangChange = new Subject();


  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));


  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [ServiziPiaoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificaService, useValue: mockNotificationService },
        { provide: TranslateService, useValue: mockTranslate },
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ServiziPiaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
