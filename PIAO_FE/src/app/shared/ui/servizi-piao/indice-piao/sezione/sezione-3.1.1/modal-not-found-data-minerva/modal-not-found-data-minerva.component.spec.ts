import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalNotFoundDataMinervaComponent } from './modal-not-found-data-minerva.component';

describe('ModalNotFoundDataMinervaComponent', () => {
  let component: ModalNotFoundDataMinervaComponent;
  let fixture: ComponentFixture<ModalNotFoundDataMinervaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalNotFoundDataMinervaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalNotFoundDataMinervaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
