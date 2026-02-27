import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from '../../../app.routes';
import { NotificaService } from '../../services/notifica.service';
import { of } from 'rxjs';
import { BaseLayoutComponent } from './base-layout.component';


describe('BaseComponentLayout', () => {
  let component: BaseLayoutComponent;
  let fixture: ComponentFixture<BaseLayoutComponent>;

  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BaseLayoutComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        { provide: NotificaService, useValue: mockNotificationService }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(BaseLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


});
