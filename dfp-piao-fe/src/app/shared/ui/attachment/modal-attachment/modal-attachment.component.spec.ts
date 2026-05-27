import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalAttachmentComponent } from './modal-attachment.component';

describe('ModalAttachmentComponent', () => {
  let component: ModalAttachmentComponent;
  let fixture: ComponentFixture<ModalAttachmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalAttachmentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalAttachmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
