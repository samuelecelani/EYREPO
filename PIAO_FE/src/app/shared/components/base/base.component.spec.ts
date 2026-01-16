import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseComponent } from './base.component';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AccountService } from '../../services/account.service';
import { NotificaService } from '../../services/notifica.service';
import { mockUtente } from '../../utils/mock';

describe('BaseComponent', () => {
  let component: BaseComponent;
  let fixture: ComponentFixture<BaseComponent>;
  let mockAccountService: any;

  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(async () => {
    mockAccountService = jasmine.createSpyObj('AccountService', ['getAccount', 'getFunzionalita']);

    await TestBed.configureTestingModule({
      imports: [BaseComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificaService, useValue: mockNotificationService },
        { provide: AccountService, useValue: mockAccountService },
      ]
    })
      .compileComponents();

    mockAccountService.getAccount.and.returnValue(of(mockUtente));
    mockAccountService.getFunzionalita.and.returnValue(of(['TEST', 'ALTRO']));

    fixture = TestBed.createComponent(BaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });



  it('isVisible true', (done) => {

    component.getVisibility('TEST').subscribe(() => {
      expect(component.isVisible).toEqual(true);
      done();
    });
  });



  it('isVisible false', (done) => {

    component.getVisibility('TEST20').subscribe(() => {
      expect(component.isVisible).toEqual(false);
      done();
    });
  });


  it('utente senza ruoloAttivo', (done) => {
    mockAccountService.getAccount.and.returnValue(of({
      nome: 'TEST'
    }));

    component.getVisibility('TEST20').subscribe(() => {
      expect(component.isVisible).toEqual(false);
      done();
    });
  });

  it('utente errore', (done) => {
    mockAccountService.getAccount.and.returnValue(throwError(() => new Error('Errore simulato')));
    mockAccountService.getFunzionalita.and.returnValue(throwError(() => new Error('Errore simulato')));

    component.getVisibility('TEST20').subscribe(() => {
      expect(component.isVisible).toEqual(false);
      done();
    });
  });


  it('ngOnDestroy', () => {
    const destroySpy = spyOn((component as any).destroy$, 'next').and.callThrough();
    const completeSpy = spyOn((component as any).destroy$, 'complete').and.callThrough();

    component.ngOnDestroy();

    expect(destroySpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

});
