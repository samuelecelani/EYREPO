import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OvpComponent } from './ovp.component';

describe('OvpComponent', () => {
  let component: OvpComponent;
  let fixture: ComponentFixture<OvpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OvpComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OvpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
