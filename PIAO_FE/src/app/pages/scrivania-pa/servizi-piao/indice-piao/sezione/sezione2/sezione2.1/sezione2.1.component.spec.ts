import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sezione21Component } from './sezione2.1.component';

describe('Sezione21Component', () => {
  let component: Sezione21Component;
  let fixture: ComponentFixture<Sezione21Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sezione21Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sezione21Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
