import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalSelezionaPaComponent } from './modal-seleziona-pa.component';

describe('ModalSelezionaPaComponent', () => {
  let component: ModalSelezionaPaComponent;
  let fixture: ComponentFixture<ModalSelezionaPaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalSelezionaPaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalSelezionaPaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
