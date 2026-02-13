import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ObiettiviPerformanceIndividualeComponent } from './obiettivi-performance-individuale.component';

describe('ObiettiviPerformanceIndividualeComponent', () => {
  let component: ObiettiviPerformanceIndividualeComponent;
  let fixture: ComponentFixture<ObiettiviPerformanceIndividualeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ObiettiviPerformanceIndividualeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ObiettiviPerformanceIndividualeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
