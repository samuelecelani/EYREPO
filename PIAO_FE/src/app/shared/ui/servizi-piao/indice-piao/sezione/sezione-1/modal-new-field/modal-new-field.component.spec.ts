import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalNewFieldComponent } from './modal-new-field.component';

describe('ModalNewFieldComponent', () => {
  let component: ModalNewFieldComponent;
  let fixture: ComponentFixture<ModalNewFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalNewFieldComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalNewFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
