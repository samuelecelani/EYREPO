import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalBodyFaseComponent } from './modal-body-fase.component';

describe('ModalBodyFaseComponent', () => {
  let component: ModalBodyFaseComponent;
  let fixture: ComponentFixture<ModalBodyFaseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalBodyFaseComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalBodyFaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
