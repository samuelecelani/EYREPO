import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IndicePiaoScrivaniaPAComponent } from './indice-piao-scrivania-pa.component';

describe('IndicePiaoScrivaniaPAComponent', () => {
  let component: IndicePiaoScrivaniaPAComponent;
  let fixture: ComponentFixture<IndicePiaoScrivaniaPAComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IndicePiaoScrivaniaPAComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(IndicePiaoScrivaniaPAComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
