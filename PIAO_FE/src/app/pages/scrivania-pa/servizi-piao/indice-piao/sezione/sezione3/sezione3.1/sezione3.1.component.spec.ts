import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sezione31Component } from './sezione3.1.component';

describe('Sezione31Component', () => {
  let component: Sezione31Component;
  let fixture: ComponentFixture<Sezione31Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sezione31Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sezione31Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
