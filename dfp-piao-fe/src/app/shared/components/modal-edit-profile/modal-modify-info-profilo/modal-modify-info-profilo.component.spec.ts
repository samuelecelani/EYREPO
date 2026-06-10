import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalModifyInfoProfiloComponent } from './modal-modify-info-profilo.component';

describe('ModalModifyInfoProfiloComponent', () => {
  let component: ModalModifyInfoProfiloComponent;
  let fixture: ComponentFixture<ModalModifyInfoProfiloComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalModifyInfoProfiloComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalModifyInfoProfiloComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
