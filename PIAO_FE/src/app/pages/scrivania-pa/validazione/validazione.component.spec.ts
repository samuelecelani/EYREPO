import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ValidazioneComponent } from './validazione.component';

describe('ValidazioneComponent', () => {
  let component: ValidazioneComponent;
  let fixture: ComponentFixture<ValidazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ValidazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ValidazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
