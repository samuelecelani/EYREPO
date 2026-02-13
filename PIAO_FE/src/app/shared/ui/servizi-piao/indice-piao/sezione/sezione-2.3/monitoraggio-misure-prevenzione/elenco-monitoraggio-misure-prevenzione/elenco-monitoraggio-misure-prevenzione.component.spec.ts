import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoMonitoraggioMisurePrevenzioneComponent } from './elenco-monitoraggio-misure-prevenzione.component';

describe('ElencoMonitoraggioMisurePrevenzioneComponent', () => {
  let component: ElencoMonitoraggioMisurePrevenzioneComponent;
  let fixture: ComponentFixture<ElencoMonitoraggioMisurePrevenzioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElencoMonitoraggioMisurePrevenzioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ElencoMonitoraggioMisurePrevenzioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
