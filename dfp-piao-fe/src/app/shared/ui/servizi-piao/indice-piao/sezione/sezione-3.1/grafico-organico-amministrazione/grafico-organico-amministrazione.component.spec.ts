import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraficoOrganicoAmministrazioneComponent } from './grafico-organico-amministrazione.component';

describe('GraficoOrganicoAmministrazioneComponent', () => {
  let component: GraficoOrganicoAmministrazioneComponent;
  let fixture: ComponentFixture<GraficoOrganicoAmministrazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GraficoOrganicoAmministrazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraficoOrganicoAmministrazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
