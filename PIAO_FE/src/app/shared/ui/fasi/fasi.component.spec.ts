import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FasiComponent } from './fasi.component';

describe('FasiComponent', () => {
  let component: FasiComponent;
  let fixture: ComponentFixture<FasiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FasiComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FasiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
