import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AzioniComponent } from './azioni.component';

describe('AzioniComponent', () => {
  let component: AzioniComponent;
  let fixture: ComponentFixture<AzioniComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AzioniComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AzioniComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
