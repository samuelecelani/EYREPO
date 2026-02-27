import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApprovazionePubblicazioneComponent } from './approvazione-pubblicazione.component';

describe('ApprovazionePubblicazioneComponent', () => {
  let component: ApprovazionePubblicazioneComponent;
  let fixture: ComponentFixture<ApprovazionePubblicazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApprovazionePubblicazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApprovazionePubblicazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
