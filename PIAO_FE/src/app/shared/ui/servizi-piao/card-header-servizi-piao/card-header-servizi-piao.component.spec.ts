import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardHeaderServiziPiaoComponent } from './card-header-servizi-piao.component';
import { TranslateService } from '@ngx-translate/core';
import { of, Subject } from 'rxjs';
import { NotificaService } from '../../../services/notifica.service';


describe('CardHeaderServiziPiaoComponent', () => {
  let component: CardHeaderServiziPiaoComponent;
  let fixture: ComponentFixture<CardHeaderServiziPiaoComponent>;

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
      imports: [CardHeaderServiziPiaoComponent],
      providers: [
        { provide: NotificaService, useValue: mockNotificationService },
        { provide: TranslateService, useValue: mockTranslate },
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CardHeaderServiziPiaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
