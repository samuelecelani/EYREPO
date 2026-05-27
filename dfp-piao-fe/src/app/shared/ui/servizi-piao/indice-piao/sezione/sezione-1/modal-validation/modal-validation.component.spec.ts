import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalValidationComponent } from './modal-validation.component';

describe('ModalValidazioneComponent', () => {
  let component: ModalValidationComponent;
  let fixture: ComponentFixture<ModalValidationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalValidationComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ModalValidationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
