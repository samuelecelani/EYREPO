import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoSottoFaseMonitoraggioComponent } from './elenco-sotto-fase-monitoraggio.component';

describe('ElencoSottoFaseMonitoraggioComponent', () => {
  let component: ElencoSottoFaseMonitoraggioComponent;
  let fixture: ComponentFixture<ElencoSottoFaseMonitoraggioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElencoSottoFaseMonitoraggioComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ElencoSottoFaseMonitoraggioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
