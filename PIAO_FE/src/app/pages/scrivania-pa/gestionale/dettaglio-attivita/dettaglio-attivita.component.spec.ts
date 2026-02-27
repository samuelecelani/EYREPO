import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DettaglioAttivitaComponent } from './dettaglio-attivita.component';

describe('DettaglioAttivitaComponent', () => {
  let component: DettaglioAttivitaComponent;
  let fixture: ComponentFixture<DettaglioAttivitaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DettaglioAttivitaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DettaglioAttivitaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
