import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FondiEuropeiComponent } from './fondi-europei.component';

describe('FondiEuropeiComponent', () => {
  let component: FondiEuropeiComponent;
  let fixture: ComponentFixture<FondiEuropeiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FondiEuropeiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FondiEuropeiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
