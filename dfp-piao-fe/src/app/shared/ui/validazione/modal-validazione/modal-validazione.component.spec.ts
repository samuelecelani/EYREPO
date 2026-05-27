import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalValidazioneComponent } from './modal-validazione.component';

describe('ModalValidazioneComponent', () => {
  let component: ModalValidazioneComponent;
  let fixture: ComponentFixture<ModalValidazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalValidazioneComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ModalValidazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
