import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ValorePubblicoAnticorruzioneComponent } from './valore-pubblico-anticorruzione.component';

describe('ValorePubblicoAnticorruzioneComponent', () => {
  let component: ValorePubblicoAnticorruzioneComponent;
  let fixture: ComponentFixture<ValorePubblicoAnticorruzioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ValorePubblicoAnticorruzioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ValorePubblicoAnticorruzioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
