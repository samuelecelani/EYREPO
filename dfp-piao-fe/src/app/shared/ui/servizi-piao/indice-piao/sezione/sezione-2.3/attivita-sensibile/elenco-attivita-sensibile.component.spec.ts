import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoAttivitaSensibileComponent } from './elenco-attivita-sensibile.component';

describe('ElencoAttivitaSensibileComponent', () => {
  let component: ElencoAttivitaSensibileComponent;
  let fixture: ComponentFixture<ElencoAttivitaSensibileComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElencoAttivitaSensibileComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ElencoAttivitaSensibileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
