import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IndicatoriComponent } from './indicatori.component';

describe('IndicatoriComponent', () => {
  let component: IndicatoriComponent;
  let fixture: ComponentFixture<IndicatoriComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IndicatoriComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IndicatoriComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
