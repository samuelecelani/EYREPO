import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalFotografiaObiettiviComponent } from './modal-fotografia-obiettivi.component';

describe('ModalFotografiaObiettiviComponent', () => {
  let component: ModalFotografiaObiettiviComponent;
  let fixture: ComponentFixture<ModalFotografiaObiettiviComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalFotografiaObiettiviComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalFotografiaObiettiviComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
