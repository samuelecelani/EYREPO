import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ButtonComponent } from './button.component';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { routes } from '../../../app.routes';
import { NotificaService } from '../../services/notifica.service';
import { DomSanitizer } from '@angular/platform-browser';

describe('ButtonComponent', () => {
  let component: ButtonComponent;
  let fixture: ComponentFixture<ButtonComponent>;

  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ButtonComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        { provide: NotificaService, useValue: mockNotificationService }
      ]
    }).compileComponents();



    fixture = TestBed.createComponent(ButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('onButtonClick', () => {
    spyOn(component.clicked, 'emit');

    component.disabled = false;
    component.loading = false;

    component.onButtonClick();

    expect(component.clicked.emit).toHaveBeenCalled()

  });


  it('icon null', () => {
    expect(component.getSanitizedIcon()).toBeNull();
  });

  it('should return SafeHtml when icon is in registry', () => {
    component.icon = 'user';
    const result = component.getSanitizedIcon();
    expect(component.icon).toEqual('user');
  });

  it('should return SafeHtml using raw string if icon not in registry', () => {
    component.icon = '<svg>raw</svg>';
    component.getSanitizedIcon();
    expect(component.icon).toEqual('<svg>raw</svg>');
  });

  it('should return SafeHtml using raw string if icon not in registry', () => {
    component.getIconClasses();
    expect(component.iconSize).toEqual('h-5 w-5');
  });

});
