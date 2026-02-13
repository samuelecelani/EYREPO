import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RisorseFinanziarieComponent } from './risorse-finanziarie.component';

describe('RisorseFinanziarieComponent', () => {
  let component: RisorseFinanziarieComponent;
  let fixture: ComponentFixture<RisorseFinanziarieComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RisorseFinanziarieComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RisorseFinanziarieComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
