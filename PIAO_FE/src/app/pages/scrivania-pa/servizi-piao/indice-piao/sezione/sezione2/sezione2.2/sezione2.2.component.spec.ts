import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sezione22Component } from './sezione2.2.component';

describe('Sezione22Component', () => {
  let component: Sezione22Component;
  let fixture: ComponentFixture<Sezione22Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sezione22Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sezione22Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
