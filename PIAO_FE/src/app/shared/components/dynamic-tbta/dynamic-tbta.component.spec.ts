import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DynamicTBTAComponent } from './dynamic-tbta.component';

describe('DynamicTBTAComponent', () => {
  let component: DynamicTBTAComponent;
  let fixture: ComponentFixture<DynamicTBTAComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DynamicTBTAComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DynamicTBTAComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
