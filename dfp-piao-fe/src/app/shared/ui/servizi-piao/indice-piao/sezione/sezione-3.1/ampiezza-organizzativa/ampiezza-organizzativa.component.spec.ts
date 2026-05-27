import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AmpiezzaOrganizzativaComponent } from './ampiezza-organizzativa.component';

describe('AmpiezzaOrganizzativaComponent', () => {
  let component: AmpiezzaOrganizzativaComponent;
  let fixture: ComponentFixture<AmpiezzaOrganizzativaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AmpiezzaOrganizzativaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AmpiezzaOrganizzativaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
