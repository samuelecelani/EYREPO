import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TranslateService } from '@ngx-translate/core';
import { of, Subject } from 'rxjs';
import { ModalComponent } from './modal.component';
import { NotificaService } from '../../services/notifica.service';


describe('ModalComponent', () => {
  let component: ModalComponent;
  let fixture: ComponentFixture<ModalComponent>;

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
      imports: [ModalComponent],
      providers: [
        { provide: NotificaService, useValue: mockNotificationService },
        { provide: TranslateService, useValue: mockTranslate },
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('closed emit', () => {
    // crea una spia per poter verificare se l'eventEmitter è stato chiamato
    spyOn(component.closed, 'emit');

    component.handleClose();

    expect(component.closed.emit).toHaveBeenCalled();
  });

  it('confirm emit', () => {
    // crea una spia per poter verificare se l'eventEmitter è stato chiamato
    spyOn(component.confirm, 'emit');

    component.handleConfirm();

    expect(component.confirm.emit).toHaveBeenCalled();
  });
});
