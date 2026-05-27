import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardComponent } from './card.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from '../../../app.routes';
import { of, Subject } from 'rxjs';
import { NotificaService } from '../../services/notifica.service';
import { TranslateService } from '@ngx-translate/core';

describe('CardComponent', () => {
  let component: CardComponent;
  let fixture: ComponentFixture<CardComponent>;

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
      imports: [CardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        { provide: NotificaService, useValue: mockNotificationService },
        { provide: TranslateService, useValue: mockTranslate },

      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('onCardClick disable true', () => {
    component.disabled = true;
    // crea una spia per poter verificare se l'eventEmitter è stato chiamato
    // in questo caso no perchè il disabled del button è a true
    spyOn(component.cardClick, 'emit');

    component.onCardClick();

    expect(component.cardClick.emit).not.toHaveBeenCalled();
  });

  it('onCardClick disable false', () => {
    component.disabled = false;
    // in questo caso si perchè il disabled del button è a false
    spyOn(component.cardClick, 'emit');

    component.onCardClick();

    expect(component.cardClick.emit).toHaveBeenCalled();
  });

});

