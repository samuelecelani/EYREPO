import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoFotografiaObiettiviComponent } from './elenco-fotografia-obiettivi.component';

describe('ElencoFotografiaObiettiviComponent', () => {
  let component: ElencoFotografiaObiettiviComponent;
  let fixture: ComponentFixture<ElencoFotografiaObiettiviComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElencoFotografiaObiettiviComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ElencoFotografiaObiettiviComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
