import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalMilestoneComponent } from './modal-milestone.component';

describe('ModalMilestoneComponent', () => {
  let component: ModalMilestoneComponent;
  let fixture: ComponentFixture<ModalMilestoneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalMilestoneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalMilestoneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
