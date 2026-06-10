import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sezione23Component } from './sezione2.3.component';

describe('Sezione23Component', () => {
  let component: Sezione23Component;
  let fixture: ComponentFixture<Sezione23Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sezione23Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sezione23Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
