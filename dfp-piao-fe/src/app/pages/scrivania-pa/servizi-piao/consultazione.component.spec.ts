import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultazioneComponent } from './consultazione.component';

describe('ConsultazioneComponent', () => {
  let component: ConsultazioneComponent;
  let fixture: ComponentFixture<ConsultazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
