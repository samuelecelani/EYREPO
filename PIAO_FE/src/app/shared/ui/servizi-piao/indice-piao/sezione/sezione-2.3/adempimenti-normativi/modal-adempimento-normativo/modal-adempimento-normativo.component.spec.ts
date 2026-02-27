import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalAdempimentoNormativoComponent } from './modal-adempimento-normativo.component';

describe('ModalAdempimentoNormativoComponent', () => {
  let component: ModalAdempimentoNormativoComponent;
  let fixture: ComponentFixture<ModalAdempimentoNormativoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalAdempimentoNormativoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalAdempimentoNormativoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
