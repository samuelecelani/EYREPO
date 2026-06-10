import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardHeaderValidazioneComponent } from './card-header-validazione.component';

describe('CardHeaderValidazioneComponent', () => {
  let component: CardHeaderValidazioneComponent;
  let fixture: ComponentFixture<CardHeaderValidazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardHeaderValidazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CardHeaderValidazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
