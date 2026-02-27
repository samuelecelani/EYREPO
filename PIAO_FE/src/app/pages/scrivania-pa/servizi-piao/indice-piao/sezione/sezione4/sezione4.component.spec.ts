import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sezione4Component } from './sezione4.component';

describe('Sezione4Component', () => {
  let component: Sezione4Component;
  let fixture: ComponentFixture<Sezione4Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sezione4Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sezione4Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
