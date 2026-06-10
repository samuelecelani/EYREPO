import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MancataCompilazioneComponent } from './mancata-compilazione.component';

describe('MancataCompilazioneComponent', () => {
  let component: MancataCompilazioneComponent;
  let fixture: ComponentFixture<MancataCompilazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MancataCompilazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MancataCompilazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
