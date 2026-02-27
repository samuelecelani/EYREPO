import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SezioneComponent } from './sezione.component';

describe('Sezione331Component', () => {
  let component: SezioneComponent;
  let fixture: ComponentFixture<SezioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SezioneComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SezioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
