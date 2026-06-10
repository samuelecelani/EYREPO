import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardHeaderGestionaleComponent } from './card-header-gestionale.component';

describe('CardHeaderGestionaleComponent', () => {
  let component: CardHeaderGestionaleComponent;
  let fixture: ComponentFixture<CardHeaderGestionaleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardHeaderGestionaleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CardHeaderGestionaleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
