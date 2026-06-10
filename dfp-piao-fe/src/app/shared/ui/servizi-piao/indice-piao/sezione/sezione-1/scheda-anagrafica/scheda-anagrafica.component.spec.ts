import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchedaAnagraficaComponent } from './scheda-anagrafica.component';

describe('SchedaAnagraficaComponent', () => {
  let component: SchedaAnagraficaComponent;
  let fixture: ComponentFixture<SchedaAnagraficaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SchedaAnagraficaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SchedaAnagraficaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
