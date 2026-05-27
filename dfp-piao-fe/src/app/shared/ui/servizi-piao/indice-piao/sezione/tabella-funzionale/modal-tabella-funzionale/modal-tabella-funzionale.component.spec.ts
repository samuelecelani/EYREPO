import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalTabellaFunzionaleComponent } from './modal-tabella-funzionale.component';

describe('ModalTabellaFunzionaleComponent', () => {
  let component: ModalTabellaFunzionaleComponent;
  let fixture: ComponentFixture<ModalTabellaFunzionaleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalTabellaFunzionaleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalTabellaFunzionaleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
