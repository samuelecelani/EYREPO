import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoAttivitaComponent } from './elenco-attivita.component';

describe('ElencoAttivitaComponent', () => {
  let component: ElencoAttivitaComponent;
  let fixture: ComponentFixture<ElencoAttivitaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElencoAttivitaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ElencoAttivitaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
