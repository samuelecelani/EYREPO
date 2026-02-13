import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalElencoAttivitaSensibileComponent } from './modal-elenco-attivita-sensibile.component';

describe('ModalElencoAttivitaSensibileComponent', () => {
  let component: ModalElencoAttivitaSensibileComponent;
  let fixture: ComponentFixture<ModalElencoAttivitaSensibileComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalElencoAttivitaSensibileComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ModalElencoAttivitaSensibileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
