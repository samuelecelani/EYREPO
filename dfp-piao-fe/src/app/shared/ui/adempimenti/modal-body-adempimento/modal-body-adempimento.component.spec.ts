import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalBodyAdempimentoComponent } from './modal-body-adempimento.component';

describe('ModalBodyAdempimentoComponent', () => {
  let component: ModalBodyAdempimentoComponent;
  let fixture: ComponentFixture<ModalBodyAdempimentoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalBodyAdempimentoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalBodyAdempimentoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
