import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestioneProfiloUtenteComponent } from './gestione-profilo-utente.component';

describe('GestioneProfiloUtenteComponent', () => {
  let component: GestioneProfiloUtenteComponent;
  let fixture: ComponentFixture<GestioneProfiloUtenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestioneProfiloUtenteComponent],
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestioneProfiloUtenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
