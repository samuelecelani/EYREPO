import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StrategieAttuativeComponent } from './strategie-attuative.component';

describe('StrategieAttuativeComponent', () => {
  let component: StrategieAttuativeComponent;
  let fixture: ComponentFixture<StrategieAttuativeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StrategieAttuativeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StrategieAttuativeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
