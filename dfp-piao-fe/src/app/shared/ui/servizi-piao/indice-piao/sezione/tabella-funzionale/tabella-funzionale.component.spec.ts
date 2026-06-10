import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TabellaFunzionaleComponent } from './tabella-funzionale.component';

describe('TabellaFunzionaleComponent', () => {
  let component: TabellaFunzionaleComponent;
  let fixture: ComponentFixture<TabellaFunzionaleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TabellaFunzionaleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TabellaFunzionaleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
