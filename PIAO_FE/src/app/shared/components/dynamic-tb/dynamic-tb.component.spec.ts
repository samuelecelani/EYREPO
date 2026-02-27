import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DynamicTBComponent } from './dynamic-tb.component';

describe('DynamicTBComponent', () => {
  let component: DynamicTBComponent;
  let fixture: ComponentFixture<DynamicTBComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DynamicTBComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DynamicTBComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
