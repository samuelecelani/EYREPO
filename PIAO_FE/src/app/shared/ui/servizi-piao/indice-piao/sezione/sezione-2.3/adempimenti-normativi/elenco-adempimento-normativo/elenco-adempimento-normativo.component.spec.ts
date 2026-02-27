import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoAdempimentoNormativoComponent } from './elenco-adempimento-normativo.component';

describe('ElencoAdempimentoNormativoComponent', () => {
  let component: ElencoAdempimentoNormativoComponent;
  let fixture: ComponentFixture<ElencoAdempimentoNormativoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElencoAdempimentoNormativoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ElencoAdempimentoNormativoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
