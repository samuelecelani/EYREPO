import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DynamicTBTBComponent } from './dynamic-tbtb.component';

describe('DynamicTBTBComponent', () => {
  let component: DynamicTBTBComponent;
  let fixture: ComponentFixture<DynamicTBTBComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DynamicTBTBComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DynamicTBTBComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
