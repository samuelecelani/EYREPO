import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TabellaGenerazionePdfComponent } from './tabella-generazione-pdf.component';

describe('TabellaGenerazionePdfComponent', () => {
  let component: TabellaGenerazionePdfComponent;
  let fixture: ComponentFixture<TabellaGenerazionePdfComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TabellaGenerazionePdfComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TabellaGenerazionePdfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
