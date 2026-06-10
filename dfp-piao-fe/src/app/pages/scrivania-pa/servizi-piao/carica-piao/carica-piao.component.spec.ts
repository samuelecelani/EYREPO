import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaricaPiaoComponent } from './carica-piao.component';

describe('CaricaPiaoComponent', () => {
  let component: CaricaPiaoComponent;
  let fixture: ComponentFixture<CaricaPiaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaricaPiaoComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CaricaPiaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
