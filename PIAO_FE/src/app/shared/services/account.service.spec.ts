
import { TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { AccountService } from './account.service';
import { Path } from '../utils/path';
import { of } from 'rxjs';
import { NotificaService } from './notifica.service';
import { mockFunzionalitaDTOHttp, mockFunzionalitaList, mockUtente, mockUtenteHttp } from '../utils/mock';

describe('AccountService', () => {
  let service: AccountService;
  let httpMock: HttpTestingController;
  const role: string[] = ['TEST'];


  const mockNotificationService = jasmine.createSpyObj('NotificaService', [
    'listenForNewNotifications',
    'disconnect',
    'unreadNotifications'
  ]);

  //restituisco al servizio 'unreadNotifications' un Observable vuoto
  mockNotificationService.unreadNotifications.and.returnValue(of([]));

  beforeEach(async () => {
    //si crea l'ecosistema per servizi e httpTesting
    await TestBed.configureTestingModule({
      providers: [
        AccountService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificaService, value: mockNotificationService },
      ]
    }).compileComponents();

    //inietta l'AccountService e l'HttpTesting
    service = TestBed.inject(AccountService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); //verifica se ci sono richieste pending
  });


  //se l'utente è già valorizzato non deve effettuare la chiamata
  it('utente già valorizzato non effettua la chiamata', (done) => {
    service.user = mockUtente;

    service.getAccount().subscribe(user => {
      expect(user).toEqual(mockUtente);
      done();
    });

    httpMock.expectNone(Path.url('/tokenized/user'));
  });

  //se l'user non è presente, procede a fare la chiamata
  it('utente non presente fa la chiamata', (done) => {
    service.user = null;

    service.getAccount().subscribe(user => {
      expect(user).toEqual(mockUtente);
      expect(service.user).toEqual(mockUtente);
      done();
    });

    const req = httpMock.expectOne(Path.url('/tokenized/user'));
    expect(req.request.method).toBe('GET');
    req.flush(mockUtenteHttp);
  });

  //se la chiamata torna null, l'user deve essere null
  it('chiamata null utente null', (done) => {
    service.user = null;

    service.getAccount().subscribe(user => {
      expect(user).toBeNull();
      expect(service.user).toBeNull();
      done();
    });

    const req = httpMock.expectOne(Path.url('/tokenized/user'));
    req.flush({ data: null });
  });

  //se la funzionalita è già valorizzato non deve effettuare la chiamata
  it('funzionalita già valorizzato non effettua la chiamata', (done) => {
    service.funzionalitaList = mockFunzionalitaList;

    service.getFunzionalita(role).subscribe(funzionalita => {
      expect(funzionalita).toEqual(mockFunzionalitaList);
      done();
    });

    httpMock.expectNone(Path.url('/funzionalita/by-ruolo'));
  });

  //se la funzionalita non è presente, procede a fare la chiamata
  it('funzionalita non presente fa la chiamata', (done) => {
    service.funzionalitaList = null;

    service.getFunzionalita(role).subscribe(funzionalita => {
      expect(funzionalita).toEqual(mockFunzionalitaList);
      expect(service.funzionalitaList).toEqual(mockFunzionalitaList);
      done();
    });

    const req = httpMock.expectOne(Path.url('/funzionalita/by-ruolo'));
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('id-spinner')).toBe('default');
    req.flush(mockFunzionalitaDTOHttp);
  });

  //se la chiamata torna null, la funzionalita deve essere []
  it('chiamata null funzionalita []', (done) => {
    service.funzionalitaList = null;

    service.getFunzionalita(role).subscribe(funzionalita => {
      expect(service.funzionalitaList).toEqual([]);
      done();
    });

    const req = httpMock.expectOne(Path.url('/funzionalita/by-ruolo'));
    req.flush({ data: null });
  });

});

