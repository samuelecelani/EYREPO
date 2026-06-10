import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sezione331Component } from './sezione3.3.1.component';

describe('Sezione331Component', () => {
  let component: Sezione331Component;
  let fixture: ComponentFixture<Sezione331Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sezione331Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sezione331Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
