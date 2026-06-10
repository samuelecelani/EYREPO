import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCategoriaObiettiviComponent } from './modal-categoria-obiettivi.component';

describe('ModalCategoriaObiettiviComponent', () => {
  let component: ModalCategoriaObiettiviComponent;
  let fixture: ComponentFixture<ModalCategoriaObiettiviComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCategoriaObiettiviComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalCategoriaObiettiviComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
