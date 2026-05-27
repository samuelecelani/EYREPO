import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionePiaoComponent } from './gestione-piao.component';

describe('GestionePiaoComponent', () => {
  let component: GestionePiaoComponent;
  let fixture: ComponentFixture<GestionePiaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionePiaoComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(GestionePiaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
