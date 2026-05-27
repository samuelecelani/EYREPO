import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraficoGanttComponent } from './grafico-gantt.component';

describe('GraficoGanttComponent', () => {
  let component: GraficoGanttComponent;
  let fixture: ComponentFixture<GraficoGanttComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GraficoGanttComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraficoGanttComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
