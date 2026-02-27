import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestioneRischioComponent } from './gestione-rischio.component';

describe('GestioneRischioComponent', () => {
  let component: GestioneRischioComponent;
  let fixture: ComponentFixture<GestioneRischioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestioneRischioComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestioneRischioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
