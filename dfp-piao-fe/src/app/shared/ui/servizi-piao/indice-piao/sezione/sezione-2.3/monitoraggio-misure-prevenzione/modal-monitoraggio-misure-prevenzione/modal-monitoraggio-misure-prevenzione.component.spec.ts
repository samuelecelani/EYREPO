import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalMonitoraggioMisurePrevenzioneComponent } from './modal-monitoraggio-misure-prevenzione.component';

describe('ModalMonitoraggioMisurePrevenzioneComponent', () => {
  let component: ModalMonitoraggioMisurePrevenzioneComponent;
  let fixture: ComponentFixture<ModalMonitoraggioMisurePrevenzioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalMonitoraggioMisurePrevenzioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalMonitoraggioMisurePrevenzioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
