import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sezione32Component } from './sezione3.2.component';

describe('Sezione32Component', () => {
  let component: Sezione32Component;
  let fixture: ComponentFixture<Sezione32Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sezione32Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sezione32Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
