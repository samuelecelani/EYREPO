import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardPortalePerformanceComponent } from './card-portale-performance.component';

describe('CardPortalePerformanceComponent', () => {
  let component: CardPortalePerformanceComponent;
  let fixture: ComponentFixture<CardPortalePerformanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardPortalePerformanceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CardPortalePerformanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
