import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScaricoMassivoPiaoComponent } from './scarico-massivo-piao.component';

describe('ScaricoMassivoPiaoComponent', () => {
  let component: ScaricoMassivoPiaoComponent;
  let fixture: ComponentFixture<ScaricoMassivoPiaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScaricoMassivoPiaoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ScaricoMassivoPiaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
