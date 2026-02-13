import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IndexComponent } from './index.component';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AccountService } from '../../shared/services/account.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { routes } from '../../app.routes';
import { NotificaService } from '../../shared/services/notifica.service';
import { WINDOW } from '../../shared/config/window-config';
import { TranslateService } from '@ngx-translate/core';

describe('IndexComponent', () => {
  let component: IndexComponent;
  let fixture: ComponentFixture<IndexComponent>;
  let mockAccountService: any;
  let mockRouter: any;
  let mockTranslate: any;
  let windowStub: Window & typeof globalThis;

  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(async () => {
    mockAccountService = jasmine.createSpyObj('AccountService', ['getAccount', 'getFunzionalita']);
    mockAccountService.getAccount.and.returnValue(of({ roles: ['ADMIN'] }));
    mockAccountService.getFunzionalita.and.returnValue(of(['TEST', 'ALTRO']));
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockTranslate = jasmine.createSpyObj('TranslateService', ['translate']);

    windowStub = {
      location: { assign(value: string) { return value } },
    } as unknown as Window & typeof globalThis

    await TestBed.configureTestingModule({
      imports: [IndexComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        { provide: NotificaService, useValue: mockNotificationService },
        { provide: AccountService, useValue: mockAccountService },
        { provide: Router, useValue: mockRouter },
        { provide: WINDOW, useValue: windowStub },
        { provide: TranslateService, useValue: mockTranslate },

      ],
    }).compileComponents();

    fixture = TestBed.createComponent(IndexComponent);
    component = fixture.componentInstance;

  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('card disable - Area pubblica se autorità è DFP ', () => {
    mockAccountService.getAccount.and.returnValue(of({ typeAuthority: 'DFP' }));
    component.ngOnInit();
    expect(component.cardNavigation[0].disabled).toEqual(true);
    expect(component.cardNavigation[1].disabled).toEqual(false);
    expect(component.cardNavigation[2].disabled).toEqual(false);
  });

  it('card disable - Area privata PA se autorità PA', () => {
    mockAccountService.getAccount.and.returnValue(of({ typeAuthority: 'PA' }));
    component.ngOnInit();
    expect(component.cardNavigation[1].disabled).toEqual(true);
  });

  it('card disable - Area privata DFP se autorità PA_CAPOFILA', () => {
    mockAccountService.getAccount.and.returnValue(of({ typeAuthority: 'PA_CAPOFILA' }));
    component.ngOnInit();
    expect(component.cardNavigation[2].disabled).toEqual(true);
  });

  it('torna in login se non ha autorità', () => {
    spyOn(windowStub.location, 'assign');
    mockAccountService.getAccount.and.returnValue(of({}));
    component.ngOnInit();
    expect(windowStub.location.assign).toHaveBeenCalled();
  });

  it('error in ngOnInit - getAccount', () => {
    spyOn(windowStub.location, 'assign');
    spyOn(console, 'error');
    mockAccountService.getAccount.and.returnValue(throwError(() => new Error('Errore')));
    component.ngOnInit();
    expect(console.error).toHaveBeenCalled();
    expect(windowStub.location.assign).toHaveBeenCalled();
  });

  it('vai a path selezionato', () => {
    const path = '/pages/area-pubblica';
    component.navigateTo(path);
    expect(mockRouter.navigate).toHaveBeenCalledWith([path]);
  });


  it('se esiste subscription permetti unsubscribe', () => {
    const mockSub = jasmine.createSpyObj('Subscription', ['unsubscribe']);
    component.subscription = mockSub;

    component.ngOnDestroy();

    expect(mockSub.unsubscribe).toHaveBeenCalled();
  });
});

