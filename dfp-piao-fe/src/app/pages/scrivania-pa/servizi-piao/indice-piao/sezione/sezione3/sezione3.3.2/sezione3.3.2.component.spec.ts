import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sezione332Component } from './sezione3.3.2.component';

describe('Sezione332Component', () => {
  let component: Sezione332Component;
  let fixture: ComponentFixture<Sezione332Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sezione332Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sezione332Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
