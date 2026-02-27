import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalPubblicazioneComponent } from './modal-pubblicazione.component';

describe('ModalPubblicazioneComponent', () => {
  let component: ModalPubblicazioneComponent;
  let fixture: ComponentFixture<ModalPubblicazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalPubblicazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalPubblicazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
