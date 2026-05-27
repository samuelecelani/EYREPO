import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalAmpiezzaOrganizzativaComponent } from './modal-ampiezza-organizzativa.component';

describe('ModalAmpiezzaOrganizzativaComponent', () => {
  let component: ModalAmpiezzaOrganizzativaComponent;
  let fixture: ComponentFixture<ModalAmpiezzaOrganizzativaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalAmpiezzaOrganizzativaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalAmpiezzaOrganizzativaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
