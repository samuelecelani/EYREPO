import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalBodyIndicatoreComponent } from './modal-body-indicatore.component';

describe('ModalBodyIndicatoreComponent', () => {
  let component: ModalBodyIndicatoreComponent;
  let fixture: ComponentFixture<ModalBodyIndicatoreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalBodyIndicatoreComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalBodyIndicatoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
